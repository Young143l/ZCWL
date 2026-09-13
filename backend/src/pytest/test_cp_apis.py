import requests
import json

# 基础URL
BASE_URL = "http://localhost:8080"

# 注册信息
REGISTER_DATA = {
    "userName": "testuser1234",
    "password": "test123456",
    "email": "test1234@example.com"
}

# 登录信息
LOGIN_DATA = {
    "userId": "testuser1234",
    "password": "test123456"
}

# 测试用的控制台项目数据 - Python
TEST_PROJECT_PYTHON = {
    "uId": "testuser1234",
    "type": "python",
    "projectName": "测试Python控制台项目",
    "message": "创建一个简单的Python控制台应用，输出Hello World"
}

# 测试用的控制台项目数据 - C++
TEST_PROJECT_CPP = {
    "uId": "testuser1234",
    "type": "cpp",
    "projectName": "测试C++控制台项目",
    "message": "创建一个简单的C++控制台应用，输出Hello World"
}

# 测试用的代码生成数据
TEST_CODE_UPDATE = {
    "code": "print('Hello World')",
    "message": "修改为输出Hello Console"
}

class CPAPITest:
    def __init__(self):
        self.token = None
        self.test_cp_id = None
    
    def register(self):
        """注册新用户"""
        print("\n=== 注册新用户 ===")
        try:
            response = requests.post(
                f"{BASE_URL}/register",
                headers={"Content-Type": "application/json"},
                json=REGISTER_DATA
            )
            if response.status_code == 201:
                data = response.json()
                print(f"✅ 注册成功，用户ID: {data.get('uId')}")
                return True
            else:
                print(f"⚠️  注册失败或用户已存在，状态码: {response.status_code}")
                # 注册失败可能是因为用户已存在，继续执行
                return True
        except Exception as e:
            print(f"❌ 注册请求异常: {str(e)}")
            return False
    
    def login(self):
        """登录获取token"""
        print("\n=== 登录 ===")
        try:
            response = requests.post(
                f"{BASE_URL}/users/login",
                headers={"Content-Type": "application/json"},
                json=LOGIN_DATA
            )
            if response.status_code == 200:
                data = response.json()
                self.token = data.get("token")
                print(f"✅ 登录成功，获取到token: {self.token[:20]}...")
                return True
            else:
                print(f"❌ 登录失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 登录请求异常: {str(e)}")
            return False
    
    def get_headers(self):
        """获取带token的请求头"""
        return {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.token}"
        }
    
    def test_get_all_projects(self):
        """测试获取所有项目列表"""
        print("\n=== 测试 GET /code ===")
        try:
            response = requests.get(
                f"{BASE_URL}/code?uId=testuser1234",
                headers=self.get_headers()
            )
            if response.status_code == 200:
                data = response.json()
                print(f"✅ 获取项目列表成功")
                print(f"项目数量: {len(data.get('list', []))}")
                return True
            else:
                print(f"❌ 获取项目列表失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 请求异常: {str(e)}")
            return False
    
    def test_create_cp_project(self):
        """测试创建控制台项目"""
        print("\n=== 测试 POST /code/cp ===")
        try:
            response = requests.post(
                f"{BASE_URL}/code/cp",
                headers=self.get_headers(),
                json=TEST_PROJECT
            )
            if response.status_code == 200:
                data = response.json()
                self.test_cp_id = data.get("cpId")
                print(f"✅ 创建控制台项目成功")
                print(f"项目ID: {self.test_cp_id}")
                print(f"项目类型: {data.get('type')}")
                print(f"生成的代码: {data.get('code')[:100]}...")
                return True
            else:
                print(f"❌ 创建控制台项目失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 请求异常: {str(e)}")
            return False
    
    def test_get_cp_project(self):
        """测试获取控制台项目信息"""
        print("\n=== 测试 GET /code/cp/:id ===")
        if not self.test_cp_id:
            print("❌ 没有测试项目ID，跳过此测试")
            return False
        
        try:
            response = requests.get(
                f"{BASE_URL}/code/cp/{self.test_cp_id}",
                headers=self.get_headers()
            )
            if response.status_code == 200:
                data = response.json()
                print(f"✅ 获取控制台项目信息成功")
                print(f"项目ID: {data.get('cpId')}")
                print(f"项目名称: {data.get('name')}")
                print(f"项目类型: {data.get('type')}")
                print(f"项目代码: {data.get('code')[:100]}...")
                return True
            else:
                print(f"❌ 获取控制台项目信息失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 请求异常: {str(e)}")
            return False
    
    def test_update_cp_project(self):
        """测试更新控制台项目代码"""
        print("\n=== 测试 POST /code/cp/:id ===")
        if not self.test_cp_id:
            print("❌ 没有测试项目ID，跳过此测试")
            return False
        
        try:
            response = requests.post(
                f"{BASE_URL}/code/cp/{self.test_cp_id}",
                headers=self.get_headers(),
                json=TEST_CODE_UPDATE
            )
            if response.status_code == 200:
                data = response.json()
                print(f"✅ 更新控制台项目代码成功")
                print(f"项目ID: {data.get('cpId')}")
                print(f"更新后的代码: {data.get('code')[:100]}...")
                return True
            else:
                print(f"❌ 更新控制台项目代码失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 请求异常: {str(e)}")
            return False
    
    def test_delete_cp_project(self):
        """测试删除控制台项目"""
        print("\n=== 测试 GET /code/cp/delete/:id ===")
        if not self.test_cp_id:
            print("❌ 没有测试项目ID，跳过此测试")
            return False
        
        try:
            response = requests.get(
                f"{BASE_URL}/code/cp/delete/{self.test_cp_id}",
                headers=self.get_headers()
            )
            if response.status_code == 200:
                data = response.json()
                print(f"✅ 删除控制台项目成功")
                print(f"删除的项目ID: {data.get('id')}")
                return True
            else:
                print(f"❌ 删除控制台项目失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 请求异常: {str(e)}")
            return False
    
    def test_create_cp_project_with_type(self, project_data):
        """测试创建控制台项目（带类型）"""
        print(f"\n=== 测试 POST /code/cp (类型: {project_data.get('type')})===")
        try:
            response = requests.post(
                f"{BASE_URL}/code/cp",
                headers=self.get_headers(),
                json=project_data
            )
            if response.status_code == 200:
                data = response.json()
                cp_id = data.get("cpId")
                print(f"✅ 创建控制台项目成功")
                print(f"项目ID: {cp_id}")
                print(f"项目类型: {data.get('type')}")
                print(f"生成的代码: {data.get('code')[:100]}...")
                return cp_id
            else:
                print(f"❌ 创建控制台项目失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return None
        except Exception as e:
            print(f"❌ 请求异常: {str(e)}")
            return None
    
    def run_all_tests(self):
        """运行所有测试"""
        print("开始测试 /code/cp 相关接口...")
        
        # 0. 注册用户
        if not self.register():
            print("❌ 注册失败，无法继续测试")
            return
        
        # 1. 登录
        if not self.login():
            print("❌ 登录失败，无法继续测试")
            return
        
        # 2. 获取所有项目列表（初始状态）
        self.test_get_all_projects()
        
        # 3. 测试创建 Python 控制台项目
        python_project_id = self.test_create_cp_project_with_type(TEST_PROJECT_PYTHON)
        if python_project_id:
            # 4. 获取所有项目列表（验证项目已创建）
            print("\n=== 测试 GET /code（创建项目后）===")
            self.test_get_all_projects()
            
            # 5. 获取 Python 控制台项目信息
            self.test_cp_id = python_project_id
            self.test_get_cp_project()
            
            # 6. 更新 Python 控制台项目代码
            self.test_update_cp_project()
            
            # 7. 再次获取 Python 项目信息验证更新
            self.test_get_cp_project()
            
            # 8. 删除 Python 控制台项目
            self.test_delete_cp_project()
        
        # 9. 测试创建 C++ 控制台项目
        cpp_project_id = self.test_create_cp_project_with_type(TEST_PROJECT_CPP)
        if cpp_project_id:
            # 10. 获取所有项目列表（验证项目已创建）
            print("\n=== 测试 GET /code（创建C++项目后）===")
            self.test_get_all_projects()
            
            # 11. 获取 C++ 控制台项目信息
            self.test_cp_id = cpp_project_id
            self.test_get_cp_project()
            
            # 12. 删除 C++ 控制台项目
            self.test_delete_cp_project()
        
        # 13. 再次获取所有项目列表验证删除
        self.test_get_all_projects()
        
        print("\n=== 所有测试完成 ===")

if __name__ == "__main__":
    tester = CPAPITest()
    tester.run_all_tests()
