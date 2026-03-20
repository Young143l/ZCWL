package com.example.zcwl.service;

import com.example.zcwl.entity.Project;
import reactor.core.publisher.Flux;
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

    /**
     * 流式处理项目相关问题
     * @param id 项目ID
     * @param question 问题内容
     * @return 流式问题解答
     */
    Flux<String> askProjectStream(Long id, String question);

    /**
     * 流式生成项目学习分析文档
     * @param id 项目ID
     * @return 流式文档内容
     */
    Flux<String> generateProjectDocStream(Long id);

    /**
     * 克隆Git项目并上传到云存储
     * @param projectName 项目名称
     * @param projectUrl 项目URL
     * @return 云存储ID（项目名称）
     */
    String cloneAndUploadProject(String projectName, String projectUrl);

    /**
     * 获取项目的文件结构（从云端）
     * @param cloudStorageId 云存储ID（项目名称）
     * @return 文件结构树
     */
    java.util.Map<String, Object> getProjectFileStructure(String cloudStorageId);
}