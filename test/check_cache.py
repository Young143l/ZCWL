"""
检查缓存文件是否创建成功
"""
import os
import sys

project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, project_root)

from src.agent_service import get_agent_service
import asyncio

async def check_cache():
    print("=" * 60)
    print("检查缓存文件")
    print("=" * 60)
    
    agent = await get_agent_service()
    cache_key = "todo/.git/doc_cache.md"
    
    print(f"\n尝试读取缓存: {cache_key}")
    try:
        result = await agent.call_mcp_tool(
            "get_object",
            {"bucket": "zcwl-project", "key": cache_key}
        )
        print(f"✅ 缓存文件存在!")
        print(f"内容长度: {len(result)} 字符")
        print(f"\n内容预览:")
        print("-" * 60)
        print(result[:500] if len(result) > 500 else result)
        if len(result) > 500:
            print("...")
    except Exception as e:
        print(f"❌ 读取失败: {e}")
    
    print("\n" + "=" * 60)
    
    # 同时列出 todo/.git/ 目录下的文件
    print("\n列出 todo/.git/ 目录内容:")
    try:
        result = await agent.call_mcp_tool(
            "list_objects",
            {"bucket": "zcwl-project", "prefix": "todo/.git/"}
        )
        if isinstance(result, list):
            for item in result:
                if isinstance(item, dict):
                    print(f"  - {item.get('Key', 'unknown')}")
        else:
            print(f"  {result}")
    except Exception as e:
        print(f"  列出失败: {e}")

if __name__ == "__main__":
    asyncio.run(check_cache())
