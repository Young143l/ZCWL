import requests
import json

# 测试正常流程
def test_normal_flow():
    print("测试正常流程...")
    
    # 测试正常注册
    print("\n测试正常注册:")
    test_data = {
        "userName": "中文测试用户127",
        "password": "Test123!",
        "email": "normal@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试正常登录
    print("\n测试正常登录:")
    test_data = {
        "userId": "中文测试用户127",
        "password": "Test123!"
    }
    response = requests.post("http://localhost:8080/users/login", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_normal_flow()
