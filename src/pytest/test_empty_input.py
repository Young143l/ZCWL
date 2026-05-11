import requests
import json

# 测试全空输入
def test_empty_input():
    print("测试全空输入...")
    
    # 测试注册接口全空输入
    print("\n测试注册接口全空输入:")
    url = "http://localhost:8080/register"
    headers = {"Content-Type": "application/json"}
    
    # 全空输入
    empty_data = {}
    response = requests.post(url, headers=headers, data=json.dumps(empty_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试登录接口全空输入
    print("\n测试登录接口全空输入:")
    url = "http://localhost:8080/users/login"
    response = requests.post(url, headers=headers, data=json.dumps(empty_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_empty_input()
