"""
配置类 - 集中管理所有配置项

所有敏感信息和配置都存储在此类中，其他模块从这里读取配置
"""


class Config:
    """七牛云配置类 - 集中管理所有配置项"""

    ak: str = "sn7675RwExFlusIAXMOueAN2Lk8SmhjsdsIbjB6P"
    sk: str = "O6EJ5weKrxi9Md7pub78DzrvAZsE9Euui2J18Frh"
    qiniu_kado_url: str = "zcwl-project.young143.top"

    region_name: str = "z0"
    endpoint_url: str = "https://s3-cn-east-1.qiniucs.com"

    api_key: str = ""
    model_name: str = "deepseek/deepseek-v3.2-251201"
    base_url: str = "https://api.qnaigc.com/v1"

    temperature: float = 0.7
    max_tokens: int = 2000
    timeout: int = 60

    host: str = "0.0.0.0"
    port: int = 8001


config = Config()
