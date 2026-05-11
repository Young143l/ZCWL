#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试Python代码输入功能
这个脚本用于测试后端是否正确处理Python的input()函数
"""

import websocket
import json
import time
import sys

def test_python_input():
    """测试Python代码中的input()功能"""
    
    # 使用实际的token（需要替换为有效的token）
    token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc3NTU3MTM3MSwiZXhwIjoxNzc1NjU3NzcxfQ.OVK9sn8jamzpsnRfOONYvSXWzTIqggzjXSENJhXWMho"
    
    # WebSocket URL
    ws_url = f"ws://localhost:8080/ws/code/cp/cp_abb5409c?token={token}"
    
    print(f"连接到 {ws_url}")
    
    # 连接WebSocket
    ws = websocket.WebSocket()
    try:
        ws.connect(ws_url)
        print("✅ 连接成功!")
        
        # 等待连接确认消息
        response = ws.recv()
        print(f"收到: {response}")
        
        # 测试代码：包含input()的Python代码
        test_code = """name = input('请输入您的姓名: ')
print(f'您好, {name}!')
age = input('请输入您的年龄: ')
print(f'您今年 {age} 岁')
"""
        
        print("\n📝 发送测试代码:")
        print(test_code)
        
        # 发送代码
        message = {
            "done": False,
            "input": test_code
        }
        ws.send(json.dumps(message))
        
        # 接收并打印响应
        print("\n📥 接收响应:")
        for i in range(10):  # 最多接收10条消息
            try:
                response = ws.recv(timeout=5)
                print(f"响应: {response}")
                
                # 解析JSON
                data = json.loads(response)
                
                # 检查是否是结束消息
                if data.get("done"):
                    print("\n✅ 执行完成")
                    break
                    
            except websocket.WebSocketTimeoutException:
                print("⏱️  等待超时，可能需要输入")
                break
            except Exception as e:
                print(f"❌ 接收错误: {e}")
                break
        
        # 如果程序等待输入，发送测试输入
        print("\n📤 发送测试输入 '张三'")
        message = {
            "done": False,
            "input": "张三"
        }
        ws.send(json.dumps(message))
        
        # 继续接收响应
        print("\n📥 继续接收响应:")
        for i in range(10):
            try:
                response = ws.recv(timeout=5)
                print(f"响应: {response}")
                
                data = json.loads(response)
                if data.get("done"):
                    print("\n✅ 执行完成")
                    break
                    
            except websocket.WebSocketTimeoutException:
                print("⏱️  等待下一个输入...")
                # 发送第二个输入
                print("\n📤 发送测试输入 '25'")
                message = {
                    "done": False,
                    "input": "25"
                }
                ws.send(json.dumps(message))
            except Exception as e:
                print(f"❌ 接收错误: {e}")
                break
        
        # 发送结束消息
        print("\n🔚 发送结束消息")
        message = {
            "done": True,
            "input": ""
        }
        ws.send(json.dumps(message))
        
        # 关闭连接
        ws.close()
        print("\n✅ 测试完成!")
        
    except Exception as e:
        print(f"❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()
    finally:
        try:
            ws.close()
        except:
            pass

if __name__ == "__main__":
    test_python_input()
