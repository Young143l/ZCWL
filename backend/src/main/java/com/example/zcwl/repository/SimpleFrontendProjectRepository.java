package com.example.zcwl.repository;

import com.example.zcwl.entity.SimpleFrontendProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 简单前端代码生成项目数据访问接口
 */
@Repository
public interface SimpleFrontendProjectRepository extends JpaRepository<SimpleFrontendProject, Long> {

    /**
     * 根据用户ID查询项目列表
     * @param userId 用户ID
     * @return 项目列表
     */
    List<SimpleFrontendProject> findByUserId(String userId);

    /**
     * 根据sfId查询项目
     * @param sfId 项目唯一标识符
     * @return 项目
     */
    Optional<SimpleFrontendProject> findBySfId(String sfId);

    /**
     * 根据项目名称和用户ID查询项目
     * @param projectName 项目名称
     * @param userId 用户ID
     * @return 项目
     */
    Optional<SimpleFrontendProject> findByProjectNameAndUserId(String projectName, String userId);
}