"""
查看 upload_text_data 工具的参数
"""
import os
import sys

project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, project_root)

from src.agent_service import get_agent_service
import asyncio

async def check_tool():
    agent = await get_agent_service()
    tools = agent.mcp_client.get_available_tools()
    
    for tool in tools:
        if tool['name'] == 'upload_text_data':
            print("工具名:", tool['name'])
            print("描述:", tool['description'])
            print("\n参数 schema:")
            import json
            print(json.dumps(tool['input_schema'], indent=2, ensure_ascii=False))
            break

if __name__ == "__main__":
    asyncio.run(check_tool())
