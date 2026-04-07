#!/usr/bin/env python3

import websocket
import json
import time
import sys

# 测试WebSocket连接
def test_websocket():
    # 使用实际的token
    token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc3NTU3MTM3MSwiZXhwIjoxNzc1NjU3NzcxfQ.OVK9sn8jamzpsnRfOONYvSXWzTIqggzjXSENJhXWMho"
    # WebSocket URL
    ws_url = f"ws://localhost:8080/ws/code/cp/cp_abb5409c?token={token}"
    
    print(f"Connecting to {ws_url}")
    
    # 连接WebSocket
    ws = websocket.WebSocket()
    try:
        ws.connect(ws_url)
        print("Connected successfully!")
        
        # 测试消息1：运行简单的Python代码
        test_code = "print('Hello, WebSocket!')\nprint('Testing code execution...')"
        message1 = {
            "done": False,
            "input": test_code
        }
        print("Sending code:")
        print(test_code)
        ws.send(json.dumps(message1))
        
        # 接收并打印响应
        print("\nReceiving response:")
        for i in range(5):  # 最多接收5条消息
            try:
                response = ws.recv()
                print(f"Response: {response}")
                # 检查是否是结束消息
                try:
                    data = json.loads(response)
                    if data.get("done"):
                        break
                except json.JSONDecodeError:
                    print("Non-JSON response received")
            except websocket.WebSocketTimeoutException:
                break
            time.sleep(1)
        
        # 测试消息2：发送结束消息
        message2 = {
            "done": True
        }
        print("\nSending stop message:")
        ws.send(json.dumps(message2))
        
        # 接收并打印响应
        print("\nReceiving stop response:")
        try:
            response = ws.recv()
            print(f"Response: {response}")
        except websocket.WebSocketTimeoutException:
            pass
        
    except Exception as e:
        print(f"Error: {e}")
    finally:
        # 关闭连接
        if ws:
            ws.close()
            print("\nConnection closed")

if __name__ == "__main__":
    # 检查是否安装了websocket-client
    try:
        import websocket
    except ImportError:
        print("Please install websocket-client: pip install websocket-client")
        sys.exit(1)
    
    test_websocket()
