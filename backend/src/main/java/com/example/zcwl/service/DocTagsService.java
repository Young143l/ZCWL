package com.example.zcwl.service;

import com.example.zcwl.entity.DocTags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 文档标签关联服务接口
 * 提供文档与标签关联关系的业务逻辑操作
 */
public interface DocTagsService {

    /**
     * 创建文档标签关系
     * @param docTag 文档标签关联实体对象，包含文档与标签的关联信息
     * @return 创建成功的文档标签关联实体对象
     */
    DocTags createDocTag(DocTags docTag);

    /**
     * 根据复合主键查询文档标签关系
     * @param id 复合主键对象，包含文档ID和标签ID
     * @return 包含文档标签关联实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<DocTags> getDocTagById(DocTags.DocTagsId id);

    /**
     * 根据文档ID查询所有文档标签关系
     * @param docId 文档ID
     * @return 该文档下所有标签关联关系的列表
     */
    List<DocTags> getDocTagsByDocId(Integer docId);

    /**
     * 根据标签ID查询所有文档标签关系
     * @param tagId 标签ID
     * @return 该标签下所有文档关联关系的列表
     */
    List<DocTags> getDocTagsByTagId(Integer tagId);

    /**
     * 查询所有文档标签关系（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含文档标签关联实体的分页结果
     */
    Page<DocTags> getAllDocTags(Pageable pageable);

    /**
     * 删除文档标签关系
     * @param id 复合主键对象，包含文档ID和标签ID
     */
    void deleteDocTag(DocTags.DocTagsId id);

}