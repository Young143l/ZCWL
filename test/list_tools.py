"""
列出所有可用的 MCP 工具
"""
import os
import sys

project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, project_root)

from src.agent_service import get_agent_service
import asyncio

async def list_tools():
    print("=" * 60)
    print("可用的 MCP 工具")
    print("=" * 60)
    
    agent = await get_agent_service()
    tools = agent.mcp_client.get_available_tools()
    
    for tool in tools:
        print(f"\n工具名: {tool['name']}")
        print(f"描述: {tool['description'][:100]}...")

if __name__ == "__main__":
    asyncio.run(list_tools())
