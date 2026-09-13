"""
七牛云存储 MCP Server 入口

使用方法:
    python run.py              启动API服务
    python run.py --check      检查MCP连接
"""

import sys
import os

if len(sys.argv) > 1 and sys.argv[1] == "--check":
    import asyncio
    from src.mcp_tool import QiniuMCPClient

    async def check():
        client = QiniuMCPClient()
        await client.connect()
        tools = client.get_available_tools()
        print(f"MCP连接成功，发现 {len(tools)} 个工具")
        for tool in tools[:5]:
            print(f"  - {tool['name']}")
        await client.disconnect()

    asyncio.run(check())
else:
    if __name__ == "__main__":
        import uvicorn
        from src.config import config

        print(f"启动服务器: http://{config.host}:{config.port}")
        print(f"API文档: http://{config.host}:{config.port}/docs")
        print(f"存储空间: zcwl-project")

        uvicorn.run(
            "src.main:app",
            host=config.host,
            port=config.port,
            reload=False,
            log_level="info"
        )
