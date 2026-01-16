package com.example.zcwl.controller;

import com.example.zcwl.dto.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * 登录接口单元测试
 * 自动化验证登录功能的各种场景
 */
@SpringBootTest // 启动Spring Boot上下文，模拟真实运行环境
@AutoConfigureMockMvc // 自动配置MockMvc，用于模拟HTTP请求
public class LoginControllerTest {

    // 模拟HTTP请求的核心工具
    @Autowired
    private MockMvc mockMvc;

    // 转换JSON的工具（把Java对象转成JSON字符串）
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试：用户ID为空（预期参数校验失败）
     */
    @Test
    public void testLoginEmptyUserId() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUserId(""); // 空用户ID
        request.setPassword("123456");

        mockMvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 预期状态码：400 参数错误或 401 未授权
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 400 || status == 401;
                });
    }

    /**
     * 测试：密码为空（预期参数校验失败）
     */
    @Test
    public void testLoginEmptyPassword() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUserId("testuser");
        request.setPassword(""); // 空密码

        mockMvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 预期状态码：400 参数错误或 401 未授权
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 400 || status == 401;
                });
    }

    /**
     * 测试：用户不存在（预期失败）
     */
    @Test
    public void testLoginUserNotFound() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUserId("nonexistentuser");
        request.setPassword("123456");

        mockMvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 预期状态码：401 未授权
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    /**
     * 测试：用户ID和密码都为空（预期参数校验失败）
     */
    @Test
    public void testLoginBothEmpty() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUserId("");
        request.setPassword("");

        mockMvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 预期状态码：400 参数错误
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * 测试：验证token接口（使用无效token）
     */
    @Test
    public void testValidateTokenInvalid() throws Exception {
        String invalidToken = "invalid-token"; 

        mockMvc.perform(MockMvcRequestBuilders.get("/validate-token")
                        .param("token", invalidToken))
                // 预期状态码：401 未授权
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    /**
     * 测试：验证token接口（无token参数）
     */
    @Test
    public void testValidateTokenMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/validate-token"))
                // 预期状态码：400 参数错误
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}

