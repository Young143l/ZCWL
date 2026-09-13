package com.example.zcwl.repository;

import com.example.zcwl.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目数据访问接口
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据用户ID查询项目列表
     * @param userId 用户ID
     * @return 项目列表
     */
    List<Project> findByUserId(String userId);

    /**
     * 根据项目名称和用户ID查询项目
     * @param projectName 项目名称
     * @param userId 用户ID
     * @return 项目
     */
    Project findByProjectNameAndUserId(String projectName, String userId);
}