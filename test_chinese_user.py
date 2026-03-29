import requests
import json

# 测试使用中文用户名注册用户
def test_register_chinese_user():
    url = "http://localhost:8080/register"
    headers = {"Content-Type": "application/json"}
    
    # 使用中文用户名
    data = {
        "userName": "中文测试用户789",
        "password": "Test123!",
        "email": "test2@example.com"
    }
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(data))
        print(f"注册用户响应状态码: {response.status_code}")
        print(f"注册用户响应内容: {response.text}")
        
        if response.status_code == 201:
            print("测试成功：中文用户名注册用户成功")
            # 测试登录
            test_login_chinese_user(data["userName"], data["password"])
        else:
            print("测试失败：中文用户名注册用户失败")
    except Exception as e:
        print(f"测试异常: {e}")

# 测试使用中文用户名登录
def test_login_chinese_user(user_id, password):
    url = "http://localhost:8080/users/login"
    headers = {"Content-Type": "application/json"}
    
    data = {
        "userId": user_id,
        "password": password
    }
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(data))
        print(f"登录响应状态码: {response.status_code}")
        print(f"登录响应内容: {response.text}")
        
        if response.status_code == 200:
            print("测试成功：中文用户名登录成功")
        else:
            print("测试失败：中文用户名登录失败")
    except Exception as e:
        print(f"测试异常: {e}")

if __name__ == "__main__":
    test_register_chinese_user()
