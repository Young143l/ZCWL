package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Project;
import com.example.zcwl.repository.ProjectRepository;
import com.example.zcwl.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> getProjects(String userId) {
        logger.debug("获取项目列表，用户ID: {}", userId);
        if (userId != null && !userId.isEmpty()) {
            return projectRepository.findByUserId(userId);
        } else {
            return projectRepository.findAll();
        }
    }

    @Override
    public Project createProject(Project project) {
        logger.debug("创建新项目: {}", project.getProjectName());
        // 检查项目名称是否已存在
        Project existingProject = projectRepository.findByProjectNameAndUserId(project.getProjectName(), project.getUserId());
        if (existingProject != null) {
            throw new IllegalArgumentException("项目名称已存在");
        }
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setStatus("active");
        return projectRepository.save(project);
    }

    @Override
    public Optional<Project> getProjectById(Long id) {
        logger.debug("获取项目信息，ID: {}", id);
        return projectRepository.findById(id);
    }

    @Override
    public boolean deleteProject(Long id, String userId) {
        logger.debug("删除项目，ID: {}, 用户ID: {}", id, userId);
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            // 验证用户权限
            if (!project.getUserId().equals(userId)) {
                throw new SecurityException("无删除权限");
            }
            projectRepository.delete(project);
            return true;
        }
        return false;
    }

    @Override
    public String askProject(Long id, String question) {
        logger.debug("询问项目问题，项目ID: {}, 问题: {}", id, question);
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("项目不存在");
        }
        // 这里应该调用AI服务处理问题，暂时返回模拟数据
        return "这是关于项目 " + projectOptional.get().getProjectName() + " 的问题回答: " + question;
    }

    @Override
    public String generateProjectDoc(Long id) {
        logger.debug("生成项目学习分析文档，项目ID: {}", id);
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("项目不存在");
        }
        Project project = projectOptional.get();
        // 这里应该调用文档生成服务，暂时返回模拟数据
        return "# 项目学习分析文档\n\n" +
               "## 项目信息\n" +
               "项目名称: " + project.getProjectName() + "\n" +
               "项目描述: " + project.getDescription() + "\n" +
               "创建时间: " + project.getCreatedAt() + "\n\n" +
               "## 学习分析\n" +
               "这是项目的学习分析内容...";
    }
}