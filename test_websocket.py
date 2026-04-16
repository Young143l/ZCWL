#!/usr/bin/env python3
"""
WebSocket测试脚本
测试后端的WebSocket功能，包括控制台和代码运行
"""

import asyncio
import json
import websockets
import aiohttp
import sys
import io

# 设置默认编码为utf-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

async def test_console_websocket():
    """测试控制台WebSocket"""
    print("测试控制台WebSocket...")
    try:
        # 连接到控制台WebSocket（通过Nginx代理）
        async with websockets.connect('ws://localhost:8081/console') as websocket:
            print("已连接到控制台WebSocket")
            
            # 接收连接成功消息
            response = await websocket.recv()
            print(f"收到消息: {response}")
            
            # 发送命令
            command = "echo Hello, World!"
            print(f"发送命令: {command}")
            await websocket.send(command)
            
            # 接收输出（设置超时）
            try:
                for _ in range(5):  # 最多接收5条消息
                    output = await asyncio.wait_for(websocket.recv(), timeout=3)
                    print(f"输出: {output}")
            except asyncio.TimeoutError:
                print("接收超时，继续测试")
            except websockets.exceptions.ConnectionClosed:
                print("连接已关闭")
            
            print("控制台WebSocket测试完成")
    except Exception as e:
        print(f"控制台WebSocket测试失败: {e}")

async def register_user():
    """注册用户"""
    print("\n注册用户...")
    try:
        async with aiohttp.ClientSession() as session:
            # 发送注册请求
            register_data = {
                "userName": "testuser123",
                "password": "Test123!",
                "email": "test123@example.com"
            }
            async with session.post('http://localhost:8080/register', json=register_data) as response:
                if response.status == 201:
                    print("注册成功")
                    return True
                else:
                    print(f"注册失败: {response.status}")
                    # 尝试获取错误信息
                    try:
                        error_data = await response.json()
                        print(f"错误信息: {error_data}")
                    except:
                        pass
                    return False
    except Exception as e:
        print(f"注册失败: {e}")
        return False

async def get_jwt_token():
    """获取JWT token"""
    print("\n获取JWT token...")
    try:
        async with aiohttp.ClientSession() as session:
            # 发送登录请求
            login_data = {
                "userId": "testuser123",
                "password": "Test123!"
            }
            async with session.post('http://localhost:8080/users/login', json=login_data) as response:
                if response.status == 200:
                    data = await response.json()
                    token = data.get("token")
                    print(f"获取token成功: {token}")
                    return token
                else:
                    print(f"登录失败: {response.status}")
                    # 尝试获取错误信息
                    try:
                        error_data = await response.json()
                        print(f"错误信息: {error_data}")
                    except:
                        pass
                    # 尝试注册用户
                    registered = await register_user()
                    if registered:
                        # 注册成功后再次尝试登录
                        async with session.post('http://localhost:8080/users/login', json=login_data) as response:
                            if response.status == 200:
                                data = await response.json()
                                token = data.get("token")
                                print(f"获取token成功: {token}")
                                return token
                    return None
    except Exception as e:
        print(f"获取token失败: {e}")
        return None

async def test_code_websocket():
    """测试代码运行WebSocket"""
    print("\n测试代码运行WebSocket...")
    try:
        # 获取JWT token
        token = await get_jwt_token()
        if not token:
            print("无法获取token，跳过代码运行WebSocket测试")
            return
        
        # 连接到代码运行WebSocket（通过Nginx代理）
        async with websockets.connect(f'ws://localhost:8081/ws/code/cp/1?token={token}') as websocket:
            print("已连接到代码运行WebSocket")
            
            # 接收连接成功消息
            response = await websocket.recv()
            print(f"收到消息: {response}")
            
            # 发送Python代码
            code = """
print("Hello, Python!")
print("这是一个测试脚本")
for i in range(3):
    print(f"数字: {i}")
print("代码运行完成")
"""
            
            # 构建消息
            message = {
                "done": False,
                "input": code
            }
            
            print("发送Python代码")
            await websocket.send(json.dumps(message))
            
            # 接收输出（设置超时）
            try:
                for _ in range(10):  # 最多接收10条消息
                    output = await asyncio.wait_for(websocket.recv(), timeout=5)
                    print(f"输出: {output}")
                    # 检查是否完成
                    try:
                        output_data = json.loads(output)
                        if output_data.get("done"):
                            break
                    except json.JSONDecodeError:
                        pass
            except asyncio.TimeoutError:
                print("接收超时，测试完成")
            except websockets.exceptions.ConnectionClosed:
                print("连接已关闭")
            
            print("代码运行WebSocket测试完成")
    except Exception as e:
        print(f"代码运行WebSocket测试失败: {e}")

async def main():
    """主函数"""
    print("开始测试WebSocket功能...")
    
    # 测试控制台WebSocket
    await test_console_websocket()
    
    # 测试代码运行WebSocket
    await test_code_websocket()
    
    print("\n所有WebSocket测试完成")

if __name__ == "__main__":
    asyncio.run(main())
