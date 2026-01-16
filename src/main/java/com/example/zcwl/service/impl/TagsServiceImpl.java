package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Tags;
import com.example.zcwl.repository.TagsRepository;
import com.example.zcwl.service.TagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 标签服务实现类
 * 实现了TagsService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class TagsServiceImpl implements TagsService {

    private final TagsRepository tagsRepository;

    /**
     * 构造函数注入
     * @param tagsRepository 标签数据访问接口实例
     */
    @Autowired  // 自动注入TagsRepository依赖，用于数据库操作
    public TagsServiceImpl(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }

    /**
     * 创建标签
     * @param tags 标签实体对象，包含标签名称
     * @return 创建成功的标签实体
     */
    @Override
    public Tags createTag(Tags tags) {
        // 调用Repository的save方法保存标签实体
        return tagsRepository.save(tags);
    }

    /**
     * 根据ID查询标签
     * @param id 标签ID
     * @return 包含标签实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<Tags> getTagById(Integer id) {
        // 调用Repository的findById方法根据ID查询
        return tagsRepository.findById(id);
    }

    /**
     * 查询所有标签（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的标签列表
     */
    @Override
    public Page<Tags> getAllTags(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return tagsRepository.findAll(pageable);
    }

    /**
     * 更新标签
     * @param id 标签ID
     * @param tags 更新后的标签信息
     * @return 更新成功的标签实体
     * @throws RuntimeException 如果标签不存在
     */
    @Override
    public Tags updateTag(Integer id, Tags tags) {
        // 先根据ID查询现有标签
        Optional<Tags> optionalTag = tagsRepository.findById(id);
        if (optionalTag.isPresent()) {
            // 如果存在，更新标签名称
            Tags existingTag = optionalTag.get();
            existingTag.setName(tags.getName());
            // 保存更新后的实体
            return tagsRepository.save(existingTag);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("Tag not found with id: " + id);
    }

    /**
     * 删除标签
     * @param id 标签ID
     */
    @Override
    public void deleteTag(Integer id) {
        // 调用Repository的deleteById方法根据ID删除
        tagsRepository.deleteById(id);
    }

    /**
     * 根据名称查询标签
     * @param name 标签名称
     * @return 匹配的标签实体，如果不存在则返回null
     */
    @Override
    public Tags getTagByName(String name) {
        // 调用Repository的findByName方法根据名称查询
        return tagsRepository.findByName(name);
    }

}