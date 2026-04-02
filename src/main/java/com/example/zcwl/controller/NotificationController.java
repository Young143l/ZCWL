package com.example.zcwl.controller;

import com.example.zcwl.entity.Notification;
import com.example.zcwl.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 获取用户未读评论消息
     * @param uId 用户ID
     * @return 未读消息列表
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam("uId") String uId) {
        logger.debug("获取用户未读消息，用户ID: {}", uId);

        // 获取用户的所有通知
        List<Notification> notifications = notificationRepository.findByuId(uId);
        List<Map<String, Object>> filteredNotifications = new ArrayList<>();

        // 构建通知列表
        for (Notification notification : notifications) {
            Map<String, Object> notificationMap = new HashMap<>();
            notificationMap.put("nId", notification.getId());
            notificationMap.put("dId", notification.getDId());
            notificationMap.put("cId", notification.getCId());
            notificationMap.put("from", notification.getFromUName());
            notificationMap.put("time", notification.getTime());
            notificationMap.put("content", notification.getContent());
            filteredNotifications.add(notificationMap);
        }

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("uId", uId);
        response.put("notifications", filteredNotifications);

        logger.info("获取用户未读消息成功，用户ID: {}, 消息数量: {}", uId, filteredNotifications.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 已读某通知（删除通知）
     * @param nId 通知ID
     * @return 成功响应
     */
    @GetMapping("/{nId}")
    public ResponseEntity<?> markAsRead(@PathVariable("nId") Long nId) {
        logger.debug("标记通知为已读，通知ID: {}", nId);

        try {
            // 删除通知
            notificationRepository.deleteById(nId);
            logger.info("标记通知为已读成功，通知ID: {}", nId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("标记通知为已读失败，通知ID: {}", nId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "标记通知为已读失败");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
