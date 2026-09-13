package com.example.zcwl.service;

import com.example.zcwl.entity.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 标签服务接口
 * 提供标签相关的业务逻辑操作
 */
public interface TagsService {

    /**
     * 创建标签
     * @param tags 标签实体对象，包含标签的基本信息
     * @return 创建成功的标签实体对象
     */
    Tags createTag(Tags tags);

    /**
     * 根据ID查询标签
     * @param id 标签ID
     * @return 包含标签实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<Tags> getTagById(Integer id);

    /**
     * 查询所有标签（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含标签实体的分页结果
     */
    Page<Tags> getAllTags(Pageable pageable);

    /**
     * 更新标签
     * @param id 标签ID
     * @param tags 包含更新信息的标签实体对象
     * @return 更新后的标签实体对象
     */
    Tags updateTag(Integer id, Tags tags);

    /**
     * 删除标签
     * @param id 标签ID
     */
    void deleteTag(Integer id);

    /**
     * 根据名称查询标签
     * @param name 标签名称
     * @return 匹配的标签实体对象，如果未找到则返回null
     */
    Tags getTagByName(String name);

}