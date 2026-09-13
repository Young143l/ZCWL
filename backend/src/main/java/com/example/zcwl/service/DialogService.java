package com.example.zcwl.service;

import com.example.zcwl.entity.Dialog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 对话服务接口
 * 提供对话相关的业务逻辑操作
 */
public interface DialogService {

    /**
     * 创建对话
     * @param dialog 对话实体对象，包含对话的基本信息
     * @return 创建成功的对话实体对象
     */
    Dialog createDialog(Dialog dialog);

    /**
     * 根据ID查询对话
     * @param id 对话ID
     * @return 包含对话实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<Dialog> getDialogById(Integer id);

    /**
     * 查询所有对话（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含对话实体的分页结果
     */
    Page<Dialog> getAllDialogs(Pageable pageable);

    /**
     * 根据用户ID查询所有对话
     * @param userId 用户ID
     * @return 该用户的所有对话列表
     */
    List<Dialog> getDialogsByUserId(String userId);

    /**
     * 更新对话
     * @param id 对话ID
     * @param dialog 包含更新信息的对话实体对象
     * @return 更新后的对话实体对象
     */
    Dialog updateDialog(Integer id, Dialog dialog);

    /**
     * 删除对话
     * @param id 对话ID
     */
    void deleteDialog(Integer id);

}