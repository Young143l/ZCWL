package com.example.zcwl.service;

import com.example.zcwl.entity.SimpleFrontendProject;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * 简单前端代码生成项目服务接口
 */
public interface SimpleFrontendProjectService {

    /**
     * 获取指定用户的简单前端代码生成项目列表
     * @param userId 用户ID
     * @return 项目列表
     */
    List<SimpleFrontendProject> getSfProjects(String userId);

    /**
     * 创建新的简单前端代码生成项目
     * @param userId 用户ID
     * @param projectName 项目名称
     * @param message 生成代码的提示信息
     * @return 创建的项目，包含生成的代码
     */
    SimpleFrontendProject createSfProject(String userId, String projectName, String message);

    /**
     * 在指定ID的项目中发起AI对话生成新的代码
     * @param sfId 项目唯一标识符
     * @param code 当前项目的代码内容
     * @param message 新的代码生成提示信息
     * @param selectId 选中的代码块ID列表
     * @return 更新后的项目，包含新生成的代码
     */
    SimpleFrontendProject generateCodeInSfProject(String sfId, Map<String, String> code, String message, List<String> selectId);

    /**
     * 获取指定ID的简易前端项目相关信息
     * @param sfId 项目唯一标识符
     * @return 项目信息
     */
    Optional<SimpleFrontendProject> getSfProjectById(String sfId);
}