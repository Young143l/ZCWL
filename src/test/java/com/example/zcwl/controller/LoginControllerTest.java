package com.example.zcwl.controller;

import com.example.zcwl.dto.LoginRequestDTO;
import com.example.zcwl.dto.LoginResponseDTO;
import com.example.zcwl.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    // 模拟AuthService，避免真实登录验证
    @MockBean
    private AuthService authService;

    /**
     * 测试：用户ID为空（预期参数校验失败）
     */
    @Test
    public void testLoginEmptyUserId() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUserId(""); // 空用户ID
        request.setPassword("123456");

        mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
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

        mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
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
        request.setPassword("12345678");

        // 模拟AuthService.login()方法对用户不存在的情况抛出异常
        Mockito.when(authService.login(Mockito.argThat(dto -> "nonexistentuser".equals(dto.getUserId()))))
                .thenThrow(new RuntimeException("Invalid userId or password"));

        mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
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

        mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
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
                // 预期状态码：401 未授权
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    /**
     * 测试：验证token接口（使用有效token）
     */
    @Test
    public void testValidateTokenValid() throws Exception {
        // 设置模拟行为，使validateToken返回true
        Mockito.when(authService.validateToken("valid-token")).thenReturn(true);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/validate-token")
                        .param("token", "valid-token"))
                // 预期状态码：200 成功
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * 测试：成功登录（预期成功）
     */
    @Test
    public void testLoginSuccess() throws Exception {
        // 准备登录请求数据
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUserId("testuser1");
        request.setPassword("12345678");

        // 准备模拟的登录响应数据
        LoginResponseDTO responseDTO = new LoginResponseDTO("test-token", "testuser1", "testuser1");

        // 模拟AuthService.login()方法返回登录响应数据
        Mockito.when(authService.login(Mockito.any(LoginRequestDTO.class))).thenReturn(responseDTO);

        // 执行登录请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 预期状态码：200 成功
                .andExpect(MockMvcResultMatchers.status().isOk())
                // 预期响应体包含token、userId和userName
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("test-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("testuser1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value("testuser1"));
    }
}