package com.example.zcwl.repository;

import com.example.zcwl.entity.ConsoleProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 控制台应用代码生成项目数据访问接口
 */
@Repository
public interface ConsoleProjectRepository extends JpaRepository<ConsoleProject, Long> {

    /**
     * 根据用户ID查询项目列表
     * @param userId 用户ID
     * @return 项目列表
     */
    List<ConsoleProject> findByUserId(String userId);

    /**
     * 根据cpId查询项目
     * @param cpId 项目唯一标识符
     * @return 项目
     */
    Optional<ConsoleProject> findByCpId(String cpId);

    /**
     * 根据项目名称和用户ID查询项目
     * @param projectName 项目名称
     * @param userId 用户ID
     * @return 项目
     */
    Optional<ConsoleProject> findByProjectNameAndUserId(String projectName, String userId);
}
