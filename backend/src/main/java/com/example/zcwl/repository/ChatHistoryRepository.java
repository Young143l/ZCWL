package com.example.zcwl.repository;

import com.example.zcwl.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天历史记录Repository接口
 * 继承JpaRepository，提供基本的CRUD操作
 * 用于操作chat_history表
 */
@Repository  // 声明这是一个Repository组件
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {

    /**
     * 根据对话ID查询历史记录
     * @param dId 对话ID
     * @return 历史记录
     */
    @Query("SELECT c FROM ChatHistory c WHERE c.dId = :dId")
    ChatHistory findByDId(Integer dId);

    /**
     * 根据对话ID查询历史记录列表
     * @param dId 对话ID
     * @return 历史记录列表
     */
    List<ChatHistory> findByDialogDId(Integer dId);

    /**
     * 根据对话ID删除历史记录
     * @param dId 对话ID
     */
    void deleteByDialogDId(Integer dId);
}
