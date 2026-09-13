"""
FastAPI主程序 - 提供问答API接口

功能:
1. 项目问答 (/ask/{id})
2. 项目问答-流式输出 (/ask/{id}/stream)
3. 生成项目文档-带缓存 (/doc/{id})
4. 并行获取代码片段
"""

import asyncio
import os
from contextlib import asynccontextmanager
from typing import List

from src.agent_service import cleanup_agent_service, get_agent_service
from src.config import config
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import json


DEFAULT_BUCKET = "zcwl-project"


class CodeSnap(BaseModel):
    """代码片段模型"""
    fileName: str = Field(description="文件名")
    lineStart: int = Field(description="起始行号")
    lineEnd: int = Field(description="结束行号")


class ProjectAskRequest(BaseModel):
    """项目问答请求模型"""
    ask: str = Field(description="用户问题")
    codeSnap: List[CodeSnap] = Field(default_factory=list, description="代码片段列表")


class ProjectAskResponse(BaseModel):
    """项目问答响应模型"""
    ans: str = Field(description="AI回答")


class ProjectDocResponse(BaseModel):
    """项目文档响应模型"""
    doc: str = Field(description="生成的学习分析文档")


async def get_project_files(agent, project_path: str) -> tuple[str, str]:
    """
    获取项目文件列表和README内容
    
    参数:
        agent: Agent服务对象
        project_path: 项目路径
        
    返回:
        tuple: (文件列表字符串, README内容字符串)
    """
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
    """应用生命周期管理"""
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
            
            # 并行获取所有代码片段
            async def fetch_code_snap(snap):
                """获取单个代码片段"""
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
                        return f"\n\n【{filename} 第{snap.lineStart}-{snap.lineEnd}行】\n" + '\n'.join(lines[start:end])
                except Exception as e:
                    return f"\n读取失败 {filename}: {e}"
                return ""
            
            # 使用 asyncio.gather 并行获取所有片段
            code_tasks = [fetch_code_snap(snap) for snap in request.codeSnap]
            code_results = await asyncio.gather(*code_tasks)
            
            # 按顺序合并结果
            for code_text in code_results:
                if code_text:
                    context += code_text
            
            context += "\n\n===== 代码片段结束 ====="

        result = await agent.ask(question=request.ask, context=context, project_url=project_path)
        return ProjectAskResponse(ans=result.get('answer', '无法获取回答'))

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ask/{id}/stream", summary="项目问答-流式输出")
async def project_ask_stream(id: str, request: ProjectAskRequest):
    """询问项目相关问题-流式返回AI回答"""
    async def generate():
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
                
                # 并行获取所有代码片段
                async def fetch_code_snap(snap):
                    """获取单个代码片段"""
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
                            return f"\n\n【{filename} 第{snap.lineStart}-{snap.lineEnd}行】\n" + '\n'.join(lines[start:end])
                    except Exception as e:
                        return f"\n读取失败 {filename}: {e}"
                    return ""
                
                # 使用 asyncio.gather 并行获取所有片段
                code_tasks = [fetch_code_snap(snap) for snap in request.codeSnap]
                code_results = await asyncio.gather(*code_tasks)
                
                # 按顺序合并结果
                for code_text in code_results:
                    if code_text:
                        context += code_text
                
                context += "\n\n===== 代码片段结束 ====="

            async for chunk in agent.ask_stream(question=request.ask, context=context, project_url=project_path):
                if chunk and chunk.strip():  # 过滤空内容
                    yield f"data: {json.dumps({'text': chunk}, ensure_ascii=False)}\n\n"
                
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)}, ensure_ascii=False)}\n\n"
    
    return StreamingResponse(generate(), media_type="text/event-stream")


@app.get("/doc/{id}", response_model=ProjectDocResponse, summary="生成项目文档")
async def project_doc(id: str):
    """生成项目学习文档（带缓存）"""
    try:
        agent = await get_agent_service()
        project_path = id if id.endswith('/') else id + '/'
        cache_key = f"{project_path}.git/doc_cache.md"
        
        # 1. 尝试读取缓存
        cache_hit = False
        cached_doc = None
        try:
            cached_doc = await agent.call_mcp_tool(
                "get_object",
                {"bucket": DEFAULT_BUCKET, "key": cache_key}
            )
            # 检查是否是有效的缓存内容（不是错误信息）
            if isinstance(cached_doc, str) and cached_doc.strip():
                # 检查是否包含错误信息
                if not cached_doc.startswith("Tool") and "error" not in cached_doc.lower() and "nosuchkey" not in cached_doc.lower():
                    cache_hit = True
                    print(f"[缓存命中] 返回缓存的文档: {cache_key}")
                else:
                    print(f"[缓存未命中] 缓存读取返回错误: {cached_doc[:100]}")
        except Exception as e:
            # 缓存不存在或读取失败
            print(f"[缓存未命中] 读取缓存异常: {e}")
        
        if cache_hit and cached_doc:
            return ProjectDocResponse(doc=cached_doc)
        
        # 2. 缓存未命中，生成文档
        print(f"[缓存未命中] 生成新文档: {project_path}")
        files, readme = await get_project_files(agent, project_path)

        question = f"""请分析这个项目并生成学习文档，包括：
1. 项目概述 2. 技术栈 3. 代码结构 4. 学习建议

项目: {project_path}
文件列表: {files}
README: {readme}"""

        result = await agent.ask(question=question, context=f"项目: {project_path}", project_url=project_path)
        doc_content = result.get('answer', '无法生成文档')
        
        # 3. 保存到缓存
        try:
            await agent.call_mcp_tool(
                "upload_text_data",
                {
                    "bucket": DEFAULT_BUCKET,
                    "key": cache_key,
                    "data": doc_content,
                    "overwrite": True
                }
            )
            print(f"[缓存已保存] {cache_key}")
        except Exception as e:
            print(f"[缓存保存失败] {e}")
            # 保存失败不影响返回结果
        
        return ProjectDocResponse(doc=doc_content)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/")
async def root():
    return {"name": "七牛云 AI 助手", "docs": "/docs"}


@app.get("/index.html")
async def frontend():
    """提供前端页面"""
    path = os.path.join(os.path.dirname(os.path.dirname(__file__)), "index.html")
    return FileResponse(path) if os.path.exists(path) else {"error": "文件不存在"}


if __name__ == "__main__":
    import uvicorn
    print(f"启动: http://{config.host}:{config.port}")
    uvicorn.run("src.main:app", host=config.host, port=config.port, reload=False)
