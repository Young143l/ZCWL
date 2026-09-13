package com.example.zcwl.service.impl;

import com.example.zcwl.entity.DocComment;
import com.example.zcwl.entity.Notification;
import com.example.zcwl.repository.DocCommentRepository;
import com.example.zcwl.repository.NotificationRepository;
import com.example.zcwl.service.DocCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档评论服务实现
 */
@Service
public class DocCommentServiceImpl implements DocCommentService {

    private static final Logger logger = LoggerFactory.getLogger(DocCommentServiceImpl.class);

    private final DocCommentRepository docCommentRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public DocCommentServiceImpl(DocCommentRepository docCommentRepository, NotificationRepository notificationRepository) {
        this.docCommentRepository = docCommentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Map<String, Object>> getComments(Long dId, Long cId) {
        logger.debug("获取评论，文档ID: {}, 章节ID: {}", dId, cId);
        
        // 获取一级评论
        List<DocComment> firstLevelComments = docCommentRepository.findFirstLevelComments(dId, cId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DocComment comment : firstLevelComments) {
            Map<String, Object> commentMap = convertCommentToMap(comment);
            
            // 获取二级评论
            List<DocComment> secondLevelComments = docCommentRepository.findByFa(comment.getId());
            List<Map<String, Object>> children = new ArrayList<>();
            
            for (DocComment childComment : secondLevelComments) {
                children.add(convertCommentToMap(childComment));
            }
            
            commentMap.put("children", children);
            result.add(commentMap);
        }
        
        logger.info("获取评论成功，文档ID: {}, 章节ID: {}, 一级评论数: {}", dId, cId, result.size());
        return result;
    }

    @Override
    public DocComment saveComment(DocComment comment) {
        logger.debug("保存评论，文档ID: {}, 章节ID: {}, 父评论ID: {}", 
                     comment.getDId(), comment.getCId(), comment.getFa());
        
        // 验证评论层级
        if (!validateCommentLevel(comment.getFa())) {
            throw new IllegalArgumentException("评论层级超过限制，最多支持两层评论");
        }
        
        // 保存评论
        DocComment savedComment = docCommentRepository.save(comment);
        logger.info("保存评论成功，评论ID: {}", savedComment.getId());
        
        // 创建通知
        createNotification(comment);
        
        return savedComment;
    }
    
    /**
     * 创建通知
     * @param comment 评论
     */
    private void createNotification(DocComment comment) {
        // 只处理二级评论（回复）
        if (comment.getFa() != -1) {
            // 获取父评论
            DocComment parentComment = docCommentRepository.findById(comment.getFa())
                    .orElseThrow(() -> new IllegalArgumentException("父评论不存在"));
            
            // 创建通知
            Notification notification = new Notification();
            notification.setUId(parentComment.getUId()); // 被回复的评论的发表者id
            notification.setDId(comment.getDId()); // 文档ID
            notification.setCId(comment.getCId()); // 章节ID
            notification.setFromUName(comment.getUId()); // 回复者id（使用email作为用户名）
            
            // 处理内容，超过10个字符则截断
            String content = comment.getComment();
            if (content.length() > 10) {
                content = content.substring(0, 10) + "...";
            }
            notification.setContent(content); // 回复内容
            notification.setTime(LocalDateTime.now()); // 回复时间
            
            // 保存通知
            notificationRepository.save(notification);
            logger.info("创建通知成功，通知ID: {}", notification.getId());
        }
    }

    @Override
    public boolean validateCommentLevel(Long fa) {
        // 如果是一级评论，合法
        if (fa == -1) {
            return true;
        }
        
        // 检查父评论是否存在
        DocComment parentComment = docCommentRepository.findById(fa)
                .orElseThrow(() -> new IllegalArgumentException("父评论不存在"));
        
        // 检查父评论是否是一级评论（fa为-1）
        if (parentComment.getFa() != -1) {
            throw new IllegalArgumentException("父评论必须是一级评论");
        }
        
        return true;
    }

    /**
     * 将评论实体转换为Map
     * @param comment 评论实体
     * @return 评论Map
     */
    private Map<String, Object> convertCommentToMap(DocComment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("uId", comment.getUId());
        map.put("email", comment.getEmail());
        map.put("content", comment.getComment());
        map.put("createdAt", comment.getCreatedAt());
        return map;
    }
}
