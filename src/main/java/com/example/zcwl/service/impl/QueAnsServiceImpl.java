package com.example.zcwl.service.impl;

import com.example.zcwl.entity.QueAns;
import com.example.zcwl.repository.QueAnsRepository;
import com.example.zcwl.service.QueAnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 问答服务实现类
 * 实现了QueAnsService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class QueAnsServiceImpl implements QueAnsService {

    private final QueAnsRepository queAnsRepository;

    /**
     * 构造函数注入
     * @param queAnsRepository 问答数据访问接口实例
     */
    @Autowired  // 自动注入QueAnsRepository依赖，用于数据库操作
    public QueAnsServiceImpl(QueAnsRepository queAnsRepository) {
        this.queAnsRepository = queAnsRepository;
    }

    /**
     * 创建问答
     * @param queAns 问答实体，包含问答内容
     * @return 创建成功的问答实体
     */
    @Override
    public QueAns createQueAns(QueAns queAns) {
        // 调用Repository的save方法保存问答实体
        return queAnsRepository.save(queAns);
    }

    /**
     * 根据复合主键查询问答
     * @param id 复合主键，包含对话ID和问答次数
     * @return 包含问答实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<QueAns> getQueAnsById(QueAns.QueAnsId id) {
        // 调用Repository的findById方法根据复合主键查询
        return queAnsRepository.findById(id);
    }

    /**
     * 根据对话ID查询所有问答
     * @param dialogId 对话ID
     * @return 该对话下的所有问答列表
     */
    @Override
    public List<QueAns> getQueAnsByDialogId(Integer dialogId) {
        // 调用Repository的自定义查询方法findByIdDId，根据对话ID查询所有问答
        return queAnsRepository.findByIdDId(dialogId);
    }

    /**
     * 查询所有问答（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的问答列表
     */
    @Override
    public Page<QueAns> getAllQueAns(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return queAnsRepository.findAll(pageable);
    }

    /**
     * 更新问答
     * @param id 复合主键，包含对话ID和问答次数
     * @param queAns 更新后的问答信息
     * @return 更新成功的问答实体
     * @throws RuntimeException 如果问答不存在
     */
    @Override
    public QueAns updateQueAns(QueAns.QueAnsId id, QueAns queAns) {
        // 先根据ID查询现有问答
        Optional<QueAns> optionalQueAns = queAnsRepository.findById(id);
        if (optionalQueAns.isPresent()) {
            // 如果存在，更新字段值
            QueAns existingQueAns = optionalQueAns.get();
            existingQueAns.setDate(queAns.getDate());  // 更新问答时间
            existingQueAns.setQue(queAns.getQue());    // 更新问题内容
            existingQueAns.setAns(queAns.getAns());    // 更新回答内容
            existingQueAns.setDialog(queAns.getDialog());  // 更新关联的对话
            // 保存更新后的实体
            return queAnsRepository.save(existingQueAns);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("QueAns not found with id: " + id);
    }

    /**
     * 删除问答
     * @param id 复合主键，包含对话ID和问答次数
     */
    @Override
    public void deleteQueAns(QueAns.QueAnsId id) {
        // 调用Repository的deleteById方法根据复合主键删除
        queAnsRepository.deleteById(id);
    }

}