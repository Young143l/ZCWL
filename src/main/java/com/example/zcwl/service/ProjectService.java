package com.example.zcwl.service;

import com.example.zcwl.entity.Project;
import java.util.List;
import java.util.Optional;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 获取项目列表，支持按用户ID筛选
     * @param userId 用户ID（可选）
     * @return 项目列表
     */
    List<Project> getProjects(String userId);

    /**
     * 创建新项目
     * @param project 项目信息
     * @return 创建的项目
     */
    Project createProject(Project project);

    /**
     * 获取指定ID的项目
     * @param id 项目ID
     * @return 项目信息
     */
    Optional<Project> getProjectById(Long id);

    /**
     * 删除指定ID的项目
     * @param id 项目ID
     * @param userId 用户ID（用于权限验证）
     * @return 删除是否成功
     */
    boolean deleteProject(Long id, String userId);

    /**
     * 处理项目相关问题
     * @param id 项目ID
     * @param question 问题内容
     * @return 问题解答
     */
    String askProject(Long id, String question);

    /**
     * 生成项目学习分析文档
     * @param id 项目ID
     * @return 文档内容
     */
    String generateProjectDoc(Long id);
}