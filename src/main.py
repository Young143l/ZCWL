"""
FastAPI主程序 - 提供问答API接口
"""

import os
from contextlib import asynccontextmanager
from typing import List

from src.agent_service import cleanup_agent_service, get_agent_service
from src.config import config
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field


DEFAULT_BUCKET = "zcwl-project"


class CodeSnap(BaseModel):
    fileName: str = Field(description="文件名")
    lineStart: int = Field(description="起始行号")
    lineEnd: int = Field(description="结束行号")


class ProjectAskRequest(BaseModel):
    ask: str = Field(description="用户问题")
    codeSnap: List[CodeSnap] = Field(default_factory=list, description="代码片段列表")


class ProjectAskResponse(BaseModel):
    ans: str = Field(description="AI回答")


class ProjectDocResponse(BaseModel):
    doc: str = Field(description="生成的学习分析文档")


async def get_project_files(agent, project_path: str) -> tuple[str, str]:
    """获取项目文件列表和README内容"""
    try:
        list_result = await agent.call_mcp_tool(
            "list_objects",
            {"bucket": DEFAULT_BUCKET, "prefix": project_path}
        )
        if isinstance(list_result, str):
            pass
        elif isinstance(list_result, list):
            file_names = []
            for item in list_result:
                if isinstance(item, dict):
                    key = item.get('Key', '')
                    if key and key != project_path:
                        file_names.append(key.replace(project_path, ''))
            if file_names:
                list_result = '\n'.join(f"- {f}" for f in file_names)
    except Exception as e:
        list_result = f"获取失败: {e}"

    readme_content = ""
    readme_keys = [
        f"{project_path}README.md",
        f"{project_path}readme.md",
        f"{project_path}README.MD",
        f"{project_path}README.txt"
    ]
    for key in readme_keys:
        try:
            result = await agent.call_mcp_tool(
                "get_object",
                {"bucket": DEFAULT_BUCKET, "key": key}
            )
            if isinstance(result, str) and result.strip():
                readme_content = result[:5000]
                break
        except:
            continue

    return list_result or "无文件", readme_content


@asynccontextmanager
async def lifespan(app: FastAPI):
    print("正在初始化Agent服务...")
    try:
        await get_agent_service()
        print("Agent服务初始化成功")
    except Exception as e:
        print(f"Agent服务初始化失败: {e}")
    yield
    print("正在清理资源...")
    await cleanup_agent_service()
    print("资源清理完成")


app = FastAPI(
    title="七牛云 AI 助手",
    description="项目问答和文档生成",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post("/ask/{id}", response_model=ProjectAskResponse, summary="项目问答")
async def project_ask(id: str, request: ProjectAskRequest):
    """询问项目相关问题"""
    try:
        agent = await get_agent_service()
        project_path = id if id.endswith('/') else id + '/'

        files, readme = await get_project_files(agent, project_path)
        context = f"""注意：你只能操作 zcwl-project 这个存储空间！
项目 "{id}" 位于 zcwl-project 桶的 {project_path} 目录下。

文件列表:
{files}

README:
{readme}"""

        if request.codeSnap:
            context += "\n\n===== 用户指定的代码片段（必须仔细分析这些代码）====="
            for snap in request.codeSnap:
                try:
                    filename = snap.fileName.lstrip('/')
                    key = project_path + filename
                    result = await agent.call_mcp_tool(
                        "get_object",
                        {"bucket": DEFAULT_BUCKET, "key": key}
                    )
                    if isinstance(result, str):
                        lines = result.split('\n')
                        start, end = max(0, snap.lineStart-1), min(len(lines), snap.lineEnd)
                        context += f"\n\n【{filename} 第{snap.lineStart}-{snap.lineEnd}行】\n" + '\n'.join(lines[start:end])
                except Exception as e:
                    context += f"\n读取失败: {e}"
            context += "\n\n===== 代码片段结束 ====="

        result = await agent.ask(question=request.ask, context=context, project_url=project_path)
        return ProjectAskResponse(ans=result.get('answer', '无法获取回答'))

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/doc/{id}", response_model=ProjectDocResponse, summary="生成项目文档")
async def project_doc(id: str):
    """生成项目学习文档"""
    try:
        agent = await get_agent_service()
        project_path = id if id.endswith('/') else id + '/'

        files, readme = await get_project_files(agent, project_path)

        question = f"""请分析这个项目并生成学习文档，包括：
1. 项目概述 2. 技术栈 3. 代码结构 4. 学习建议

项目: {project_path}
文件列表: {files}
README: {readme}"""

        result = await agent.ask(question=question, context=f"项目: {project_path}", project_url=project_path)
        return ProjectDocResponse(doc=result.get('answer', '无法生成文档'))

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/")
async def root():
    return {"name": "七牛云 AI 助手", "docs": "/docs"}


@app.get("/index.html")
async def frontend():
    path = os.path.join(os.path.dirname(os.path.dirname(__file__)), "index.html")
    return FileResponse(path) if os.path.exists(path) else {"error": "文件不存在"}


if __name__ == "__main__":
    import uvicorn
    print(f"启动: http://{config.host}:{config.port}")
    uvicorn.run("main:app", host=config.host, port=config.port, reload=False)
