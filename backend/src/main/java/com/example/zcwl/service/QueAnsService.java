package com.example.zcwl.service;

import com.example.zcwl.entity.QueAns;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 问答服务接口
 * 定义了问答相关的业务逻辑方法
 */
public interface QueAnsService {

    /**
     * 创建问答
     * @param queAns 问答实体，包含问答内容
     * @return 创建成功的问答实体
     */
    QueAns createQueAns(QueAns queAns);

    /**
     * 根据复合主键查询问答
     * @param id 复合主键，包含对话ID和问答次数
     * @return 包含问答实体的Optional对象，如果不存在则返回Optional.empty()
     */
    Optional<QueAns> getQueAnsById(QueAns.QueAnsId id);

    /**
     * 根据对话ID查询所有问答
     * @param dialogId 对话ID
     * @return 该对话下的所有问答列表
     */
    List<QueAns> getQueAnsByDialogId(Integer dialogId);

    /**
     * 查询所有问答（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的问答列表
     */
    Page<QueAns> getAllQueAns(Pageable pageable);

    /**
     * 更新问答
     * @param id 复合主键，包含对话ID和问答次数
     * @param queAns 更新后的问答信息
     * @return 更新成功的问答实体
     * @throws RuntimeException 如果问答不存在
     */
    QueAns updateQueAns(QueAns.QueAnsId id, QueAns queAns);

    /**
     * 删除问答
     * @param id 复合主键，包含对话ID和问答次数
     */
    void deleteQueAns(QueAns.QueAnsId id);

}