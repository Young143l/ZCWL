package com.example.zcwl.repository;

import com.example.zcwl.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 通知Repository
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据用户ID查询通知
     * @param uId 用户ID
     * @return 通知列表
     */
    List<Notification> findByuId(String uId);
}
