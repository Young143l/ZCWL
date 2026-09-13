"""
MCP客户端模块 - 封装七牛MCP Server为LangChain工具
"""

import asyncio
import json
import os
import shutil
from typing import Any, Dict, List, Optional, Type

from langchain_core.tools import BaseTool
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
from pydantic import BaseModel, Field, create_model

from src.config import config


def _get_uvx_command():
    """获取uvx命令"""
    if shutil.which("uvx"):
        return "uvx"
    if shutil.which("uv"):
        return "uv"
    raise RuntimeError("未找到 uvx 或 uv 命令，请运行: pip install uv")


class QiniuMCPClient:

    def __init__(self):
        self.ak = config.ak
        self.sk = config.sk
        self.session: Optional[ClientSession] = None
        self._stdio_context = None
        self._session_context = None
        self._tools_cache: List[Dict[str, Any]] = []
        self._connected = False

    async def connect(self) -> None:
        if self._connected:
            return

        env = os.environ.copy()
        env["QINIU_ACCESS_KEY"] = self.ak
        env["QINIU_SECRET_KEY"] = self.sk
        env["QINIU_REGION_NAME"] = config.region_name
        env["QINIU_ENDPOINT_URL"] = config.endpoint_url

        uvx_cmd = _get_uvx_command()
        print(f"使用命令: {uvx_cmd} qiniu-mcp-server")

        server_params = StdioServerParameters(
            command=uvx_cmd,
            args=["qiniu-mcp-server"],
            env=env
        )

        self._stdio_context = stdio_client(server_params)
        read_stream, write_stream = await self._stdio_context.__aenter__()

        self.session = ClientSession(read_stream, write_stream)
        await self.session.__aenter__()
        await self.session.initialize()

        tools_result = await self.session.list_tools()
        self._tools_cache = [
            {
                "name": tool.name,
                "description": tool.description or "",
                "input_schema": tool.inputSchema
            }
            for tool in tools_result.tools
        ]

        self._connected = True
        print(f"MCP连接成功，发现 {len(self._tools_cache)} 个工具")

    async def disconnect(self) -> None:
        if not self._connected:
            return

        try:
            if self.session:
                await self.session.__aexit__(None, None, None)
                self.session = None
            if self._stdio_context:
                await self._stdio_context.__aexit__(None, None, None)
                self._stdio_context = None
        except Exception as e:
            print(f"断开MCP连接时出错: {e}")
        finally:
            self._connected = False

    async def call_tool(self, tool_name: str, arguments: Dict[str, Any]) -> Any:
        if not self.session or not self._connected:
            raise RuntimeError("MCP会话未初始化，请先调用connect()")

        result = await self.session.call_tool(tool_name, arguments)

        if result.content:
            text_contents = [
                content.text for content in result.content
                if hasattr(content, 'text')
            ]
            if text_contents:
                return "\n".join(text_contents)

        return result.model_dump()

    def get_available_tools(self) -> List[Dict[str, Any]]:
        return self._tools_cache

    @property
    def is_connected(self) -> bool:
        return self._connected


def create_args_model(schema: Dict[str, Any]) -> Type[BaseModel]:
    if not schema or "properties" not in schema:
        return create_model("EmptyArgs")

    properties = schema.get("properties", {})
    required = set(schema.get("required", []))

    fields = {}
    for name, prop in properties.items():
        prop_type = prop.get("type", "string")

        if prop_type == "string":
            field_type = str
        elif prop_type == "integer":
            field_type = int
        elif prop_type == "number":
            field_type = float
        elif prop_type == "boolean":
            field_type = bool
        elif prop_type == "array":
            field_type = list
        elif prop_type == "object":
            field_type = dict
        else:
            field_type = str

        if name in required:
            fields[name] = (field_type, Field(..., description=prop.get("description", "")))
        else:
            default = prop.get("default", None)
            fields[name] = (Optional[field_type], Field(default=default, description=prop.get("description", "")))

    return create_model("ToolArgs", **fields)


class MCPToolWrapper(BaseTool):
    name: str = Field(description="工具名称")
    description: str = Field(default="", description="工具描述")
    mcp_client: Optional[QiniuMCPClient] = Field(default=None, description="MCP客户端实例")

    class Config:
        arbitrary_types_allowed = True

    def _run(self, **kwargs) -> str:
        raise NotImplementedError("MCP工具仅支持异步调用，请使用arun方法")

    async def _arun(self, **kwargs) -> str:
        if not self.mcp_client:
            return "错误: MCP客户端未初始化"

        try:
            arguments = {k: v for k, v in kwargs.items() if v is not None}
            result = await self.mcp_client.call_tool(self.name, arguments)

            if isinstance(result, str):
                return result
            return json.dumps(result, ensure_ascii=False, indent=2)

        except Exception as e:
            return f"工具调用失败: {str(e)}"


async def create_mcp_tools() -> tuple[QiniuMCPClient, List[MCPToolWrapper]]:
    client = QiniuMCPClient()
    await client.connect()

    tools_info = client.get_available_tools()
    langchain_tools: List[MCPToolWrapper] = []

    for tool_info in tools_info:
        args_schema = create_args_model(tool_info.get("input_schema", {}))

        wrapper = MCPToolWrapper(
            name=tool_info["name"],
            description=tool_info["description"],
            args_schema=args_schema,
            mcp_client=client
        )
        langchain_tools.append(wrapper)

    return client, langchain_tools
