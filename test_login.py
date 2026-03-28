import requests
import json

# 测试基础URL
BASE_URL = "http://localhost:8080"

# 测试中文用户登录
def test_chinese_login():
    print("测试中文用户登录")
    url = f"{BASE_URL}/users/login"
    headers = {"Content-Type": "application/json"}
    
    # 测试用例1：中文用户名正常登录
    print("测试用例1：中文用户名正常登录")
    data = {
        "userId": "新的中文用户123",
        "password": "Password123!"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    print(f"响应头: {dict(response.headers)}")

if __name__ == "__main__":
    test_chinese_login()