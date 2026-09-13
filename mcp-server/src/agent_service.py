"""
Agent服务模块 - LangChain Agent初始化和管理
"""

from typing import Any, Dict, List, Optional, AsyncGenerator

from langchain_classic.agents import AgentExecutor, create_tool_calling_agent
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI

from src.config import config
from src.mcp_tool import MCPToolWrapper, QiniuMCPClient, create_mcp_tools


class AgentService:

    def __init__(self):
        self.ak = config.ak
        self.sk = config.sk
        self.api_key = config.api_key
        self.model_name = config.model_name
        self.base_url = config.base_url
        self.temperature = config.temperature

        self.mcp_client: Optional[QiniuMCPClient] = None
        self.tools: List[MCPToolWrapper] = []
        self.llm: Optional[ChatOpenAI] = None
        self.agent: Optional[Any] = None
        self.agent_executor: Optional[AgentExecutor] = None
        self._initialized = False

    async def initialize(self) -> None:
        if self._initialized:
            return

        self.mcp_client, self.tools = await create_mcp_tools()

        self.llm = ChatOpenAI(
            model=self.model_name,
            api_key=self.api_key,
            base_url=self.base_url,
            temperature=self.temperature,
            streaming=True
        )

        prompt = ChatPromptTemplate.from_messages([
            ("system", """你是一个专业的项目代码助手，专门分析存储在七牛云 zcwl-project 存储空间中的项目。

重要规则：
1. 用户已经在下方提供了项目文件列表、README内容和用户指定的代码片段
2. 基于这些信息直接回答问题，除非必要，不要再次调用工具查询文件内容
3. 如果用户问题涉及特定文件的详细内容，而上下文没有提供，可以调用工具获取该文件
4. 只能使用 zcwl-project 存储空间，项目路径是 zcwl-project 桶中的文件夹名

工具调用策略：
1. 优先使用用户提供的上下文信息回答问题
2. 只有当用户问题涉及的文件内容不在提供的上下文中时，才调用工具获取
3. 避免重复获取用户已经提供的代码片段
4. 对于项目结构问题，可以调用list_objects了解文件组织
5. 对于具体文件内容问题，调用get_object获取文件内容

分析框架：
1. 项目理解：基于文件列表和README，理解项目类型、主要功能和架构
2. 代码分析：仔细分析用户提供的代码片段，理解其功能、逻辑和实现方式
3. 技术栈识别：从文件扩展名、依赖文件等识别使用的技术栈
4. 问题解答：针对用户的具体问题，提供准确、实用的回答

回答要求：
1. 针对问题直接回答，避免无关的背景介绍
2. 回答要结构清晰，可以使用适当的格式（如列表、代码块等）
3. 对于代码问题，提供具体的解释和建议
4. 对于架构问题，分析项目结构并提出改进建议
5. 对于学习建议，提供有针对性的学习路径
6. 如果不确定或信息不足，明确说明需要哪些额外信息
7. 避免猜测，基于现有信息给出合理推断

当前上下文信息: {context}"""),
            ("human", "{input}"),
            ("placeholder", "{agent_scratchpad}")
        ])

        self.agent = create_tool_calling_agent(self.llm, self.tools, prompt)

        self.agent_executor = AgentExecutor(
            agent=self.agent,
            tools=self.tools,
            verbose=False,
            handle_parsing_errors=True,
            max_iterations=20
        )

        self._initialized = True

    async def call_mcp_tool(self, tool_name: str, arguments: Dict[str, Any]) -> Any:
        if not self._initialized or not self.mcp_client:
            await self.initialize()
        return await self.mcp_client.call_tool(tool_name, arguments)

    async def ask(
        self,
        question: str,
        context: str = "",
        project_url: str = ""
    ) -> Dict[str, Any]:
        if not self._initialized:
            await self.initialize()

        try:
            agent_input = {
                "input": question,
                "context": context or "无额外上下文"
            }

            result = await self.agent_executor.ainvoke(agent_input)

            tool_calls = []
            if "intermediate_steps" in result:
                for step in result["intermediate_steps"]:
                    if len(step) >= 2:
                        action, observation = step[0], step[1]
                        tool_calls.append({
                            "tool": action.tool,
                            "input": action.tool_input,
                            "output": str(observation)[:500]
                        })

            return {
                "success": True,
                "answer": result.get("output", "无法获取回答"),
                "tool_calls": tool_calls
            }

        except Exception as e:
            return {
                "success": False,
                "answer": f"处理请求时发生错误: {str(e)}",
                "tool_calls": []
            }

    async def ask_stream(
        self,
        question: str,
        context: str = "",
        project_url: str = ""
    ) -> AsyncGenerator[str, None]:
        """
        流式输出AI回答
        
        直接使用LLM的流式功能，实现真正的逐字输出
        """
        if not self._initialized:
            await self.initialize()

        try:
            # 构建提示消息
            messages = [
                ("system", f"""你是一个专业的项目代码助手，专门分析存储在七牛云 zcwl-project 存储空间中的项目。

重要规则：
1. 用户已经在下方提供了项目文件列表、README内容和用户指定的代码片段
2. 基于这些信息直接回答问题，除非必要，不要再次查询文件内容
3. 只能使用 zcwl-project 存储空间

当前上下文信息:
{context}"""),
                ("human", question)
            ]
            
            # 使用LLM的流式输出
            async for chunk in self.llm.astream(messages):
                if chunk.content and chunk.content.strip():  # 过滤空内容
                    yield chunk.content

        except Exception as e:
            yield f"处理请求时发生错误: {str(e)}"

    async def cleanup(self) -> None:
        if self.mcp_client:
            await self.mcp_client.disconnect()
        self._initialized = False


_agent_service: Optional[AgentService] = None


async def get_agent_service() -> AgentService:
    global _agent_service

    if _agent_service is None:
        _agent_service = AgentService()
        await _agent_service.initialize()

    return _agent_service


async def cleanup_agent_service() -> None:
    global _agent_service
    if _agent_service:
        await _agent_service.cleanup()
        _agent_service = None
