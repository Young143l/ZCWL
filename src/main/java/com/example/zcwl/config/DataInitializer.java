package com.example.zcwl.config;

import com.example.zcwl.entity.User;
import com.example.zcwl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化类
 * 在应用启动时向数据库中添加测试用户
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            // 暂时注释掉测试用户初始化代码
            /*
            // 无论数据库中是否已有用户，都添加或更新测试用户
            
            // 添加或更新测试用户1
            User user1 = userRepository.findByuId("1");
            if (user1 == null) {
                user1 = new User();
                user1.setUId("1");
            }
            user1.setName("张三");
            user1.setEmail("zhangsan@example.com");
            user1.setPassword("123456"); // 使用明文密码存储（由于数据库表结构限制）
            userRepository.save(user1);

            // 添加或更新测试用户2
            User user2 = userRepository.findByuId("2");
            if (user2 == null) {
                user2 = new User();
                user2.setUId("2");
            }
            user2.setName("李四");
            user2.setEmail("lisi@example.com");
            user2.setPassword("123456"); // 使用明文密码存储（由于数据库表结构限制）
            userRepository.save(user2);

            // 添加或更新测试用户3
            User user3 = userRepository.findByuId("3");
            if (user3 == null) {
                user3 = new User();
                user3.setUId("3");
            }
            user3.setName("王五");
            user3.setEmail("wangwu@example.com");
            user3.setPassword("123456"); // 使用明文密码存储（由于数据库表结构限制）
            userRepository.save(user3);

            System.out.println("测试用户添加或更新成功！");
            */
            System.out.println("测试用户初始化已禁用");
        } catch (Exception e) {
            System.out.println("测试用户添加或更新失败：" + e.getMessage());
            // 捕获异常，确保应用能够正常启动
        }
    }
}