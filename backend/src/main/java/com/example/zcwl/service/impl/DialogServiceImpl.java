package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Dialog;
import com.example.zcwl.repository.DialogRepository;
import com.example.zcwl.service.DialogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 对话服务实现类
 * 实现了DialogService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class DialogServiceImpl implements DialogService {

    private final DialogRepository dialogRepository;

    /**
     * 构造函数注入
     * @param dialogRepository 对话数据访问接口实例
     */
    @Autowired  // 自动注入DialogRepository依赖，用于数据库操作
    public DialogServiceImpl(DialogRepository dialogRepository) {
        this.dialogRepository = dialogRepository;
    }

    /**
     * 创建对话
     * @param dialog 对话实体对象，包含对话基本信息
     * @return 创建成功的对话实体
     */
    @Override
    public Dialog createDialog(Dialog dialog) {
        // 调用Repository的save方法保存对话实体
        return dialogRepository.save(dialog);
    }

    /**
     * 根据ID查询对话
     * @param id 对话ID
     * @return 包含对话实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<Dialog> getDialogById(Integer id) {
        // 调用Repository的findById方法根据ID查询
        return dialogRepository.findById(id);
    }

    /**
     * 查询所有对话（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的对话列表
     */
    @Override
    public Page<Dialog> getAllDialogs(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return dialogRepository.findAll(pageable);
    }

    /**
     * 根据用户ID查询所有对话
     * @param userId 用户ID
     * @return 该用户的所有对话列表
     */
    @Override
    public List<Dialog> getDialogsByUserId(String userId) {
        // 调用Repository的findByUserUId方法根据用户ID查询
        return dialogRepository.findByUserUId(userId);
    }

    /**
     * 更新对话
     * @param id 对话ID
     * @param dialog 更新后的对话信息
     * @return 更新成功的对话实体
     * @throws RuntimeException 如果对话不存在
     */
    @Override
    public Dialog updateDialog(Integer id, Dialog dialog) {
        // 先根据ID查询现有对话
        Optional<Dialog> optionalDialog = dialogRepository.findById(id);
        if (optionalDialog.isPresent()) {
            // 如果存在，更新对话信息
            Dialog existingDialog = optionalDialog.get();
            existingDialog.setQaTimes(dialog.getQaTimes());
            existingDialog.setdAbstract(dialog.getdAbstract());
            existingDialog.setUser(dialog.getUser());
            // 保存更新后的实体
            return dialogRepository.save(existingDialog);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("Dialog not found with id: " + id);
    }

    /**
     * 删除对话
     * @param id 对话ID
     */
    @Override
    public void deleteDialog(Integer id) {
        // 调用Repository的deleteById方法根据ID删除
        dialogRepository.deleteById(id);
    }

}