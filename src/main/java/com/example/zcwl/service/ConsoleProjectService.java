package com.example.zcwl.service;

import com.example.zcwl.entity.ConsoleProject;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Optional;

/**
 * 控制台应用代码生成项目服务接口
 */
public interface ConsoleProjectService {

    /**
     * 获取控制台应用代码生成项目列表，可指定用户
     * @param userId 用户ID，可为null
     * @return 项目列表
     */
    List<ConsoleProject> getCpProjects(String userId);

    /**
     * 创建新的控制台应用代码生成项目
     * @param userId 用户ID
     * @param projectName 项目名称
     * @param message 生成代码的提示信息
     * @return 创建的项目，包含生成的代码
     */
    ConsoleProject createCpProject(String userId, String projectName, String message);

    /**
     * 在指定ID的项目中发起AI对话生成新的代码
     * @param cpId 项目唯一标识符
     * @param code 当前项目的代码内容
     * @param message 新的代码生成提示信息
     * @param selectId 选中的代码块ID列表
     * @return 更新后的项目，包含新生成的代码
     */
    ConsoleProject generateCodeInCpProject(String cpId, String code, String message, List<String> selectId);

    /**
     * 获取指定ID的控制台项目相关信息
     * @param cpId 项目唯一标识符
     * @return 项目信息
     */
    Optional<ConsoleProject> getCpProjectById(String cpId);

    /**
     * 流式生成代码
     * @param message 生成代码的提示信息
     * @return 流式响应的Flux
     */
    Flux<String> generateCodeStream(String message);

    /**
     * 在指定ID的项目中流式生成新的代码
     * @param cpId 项目唯一标识符
     * @param code 当前项目的代码内容
     * @param message 新的代码生成提示信息
     * @return 流式响应的Flux
     */
    Flux<String> generateCodeInCpProjectStream(String cpId, String code, String message);

    /**
     * 删除指定ID的控制台应用代码生成项目
     * @param cpId 项目唯一标识符
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteCpProject(String cpId, String userId);
}
