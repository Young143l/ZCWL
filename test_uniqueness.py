import requests
import json

# 测试唯一性
def test_uniqueness():
    print("测试唯一性...")
    
    # 测试用户名已存在
    print("\n测试用户名已存在:")
    test_data = {
        "userName": "中文测试用户127",  # 已存在的用户名
        "password": "Test123!",
        "email": "unique1@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试邮箱已存在
    print("\n测试邮箱已存在:")
    test_data = {
        "userName": "中文测试用户128",  # 新用户名
        "password": "Test123!",
        "email": "normal@example.com"  # 已存在的邮箱
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_uniqueness()
