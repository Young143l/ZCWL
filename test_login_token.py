#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试脚本：测试登录获取token功能
"""

import requests
import json
import time

# 服务器地址
BASE_URL = "http://localhost:8080"

# 测试用户信息
TEST_USERNAME = f"testuser_{int(time.time())}"
TEST_PASSWORD = "password123"
TEST_EMAIL = f"t{int(time.time())}@ex.com"


def test_register():
    """测试用户注册"""
    print("\n=== 测试用户注册 ===")
    url = f"{BASE_URL}/register"
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "username": TEST_USERNAME,
        "password": TEST_PASSWORD,
        "email": TEST_EMAIL
    }
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(data))
        print(f"注册响应状态码: {response.status_code}")
        print(f"注册响应内容: {response.text}")
        
        if response.status_code == 201:
            print("✅ 注册成功")
            return True
        else:
            print("❌ 注册失败")
            return False
    except Exception as e:
        print(f"❌ 注册请求异常: {e}")
        return False


def test_login():
    """测试用户登录获取token"""
    print("\n=== 测试用户登录获取token ===")
    url = f"{BASE_URL}/users/login"
    headers = {
        "Content-Type": "application/json"
    }
    data = {
        "userId": TEST_USERNAME,
        "password": TEST_PASSWORD
    }
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(data))
        print(f"登录响应状态码: {response.status_code}")
        print(f"登录响应内容: {response.text}")
        
        if response.status_code == 200:
            print("✅ 登录成功")
            response_json = response.json()
            token = response_json.get("token")
            if token:
                print(f"✅ 获取到token: {token}")
                return token
            else:
                print("❌ 登录响应中没有token字段")
                return None
        else:
            print("❌ 登录失败")
            return None
    except Exception as e:
        print(f"❌ 登录请求异常: {e}")
        return None


def main():
    """主测试函数"""
    print("开始测试登录获取token功能...")
    
    # 1. 测试注册
    register_success = test_register()
    if not register_success:
        print("注册失败，测试结束")
        return
    
    # 2. 测试登录获取token
    token = test_login()
    if token:
        print("\n=== 测试完成 ===")
        print(f"🎉 成功获取到token: {token}")
    else:
        print("\n=== 测试失败 ===")
        print("❌ 未能获取到token")


if __name__ == "__main__":
    main()
