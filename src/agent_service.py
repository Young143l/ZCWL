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
            ("system", """你是一个专业的项目代码助手。

重要规则：
1. 用户已经在下方提供了项目文件列表和代码片段
2. 直接分析这些内容来回答用户问题，不要再次调用工具查询
3. 只能使用 zcwl-project 存储空间
4. 用户提供的项目名是 zcwl-project 桶中的文件夹名

回答要求：
1. 直接回答问题，不要长篇大论分析
2. 言简意赅，突出重点
3. 如果不确定就说不知道，不要猜测

当前上下文信息: {context}"""),
            ("human", "{input}"),
            ("placeholder", "{agent_scratchpad}")
        ])

        self.agent = create_tool_calling_agent(self.llm, self.tools, prompt)

        self.agent_executor = AgentExecutor(
            agent=self.agent,
            tools=self.tools,
            verbose=True,
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
        if not self._initialized:
            await self.initialize()

        try:
            agent_input = {
                "input": question,
                "context": context or "无额外上下文"
            }

            async for chunk in self.agent_executor.astream(agent_input):
                if "actions" in chunk:
                    for action in chunk["actions"]:
                        yield f"[调用工具: {action.tool}]\n"
                elif "steps" in chunk:
                    pass
                elif "output" in chunk:
                    output = chunk["output"]
                    if isinstance(output, str):
                        yield output
                    else:
                        yield str(output)

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
