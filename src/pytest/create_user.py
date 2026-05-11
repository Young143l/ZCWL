import requests
import json

# 测试基础URL
BASE_URL = "http://localhost:8080"

# 创建测试用户
def create_test_user():
    print("创建测试用户")
    url = f"{BASE_URL}/users"
    headers = {"Content-Type": "application/json"}
    
    # 测试用例：创建测试用户
    data = {
        "userName": "testuser",
        "password": "Test12345678!",
        "email": "testuser@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    create_test_user()