package com.example.zcwl.service.impl;

import com.example.zcwl.entity.DocTags;
import com.example.zcwl.repository.DocTagsRepository;
import com.example.zcwl.service.DocTagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 文档标签关联服务实现类
 * 实现了DocTagsService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class DocTagsServiceImpl implements DocTagsService {

    private final DocTagsRepository docTagsRepository;

    /**
     * 构造函数注入
     * @param docTagsRepository 文档标签关联数据访问接口实例
     */
    @Autowired  // 自动注入DocTagsRepository依赖，用于数据库操作
    public DocTagsServiceImpl(DocTagsRepository docTagsRepository) {
        this.docTagsRepository = docTagsRepository;
    }

    /**
     * 创建文档标签关系
     * @param docTag 文档标签关联实体对象，包含文档与标签的关联信息
     * @return 创建成功的文档标签关联实体
     */
    @Override
    public DocTags createDocTag(DocTags docTag) {
        // 调用Repository的save方法保存文档标签关联实体
        return docTagsRepository.save(docTag);
    }

    /**
     * 根据复合主键查询文档标签关系
     * @param id 复合主键对象，包含文档ID和标签ID
     * @return 包含文档标签关联实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<DocTags> getDocTagById(DocTags.DocTagsId id) {
        // 调用Repository的findById方法根据复合主键查询
        return docTagsRepository.findById(id);
    }

    /**
     * 根据文档ID查询所有文档标签关系
     * @param docId 文档ID
     * @return 该文档下所有标签关联关系的列表
     */
    @Override
    public List<DocTags> getDocTagsByDocId(Integer docId) {
        // 调用Repository的findByIdDocId方法根据文档ID查询
        return docTagsRepository.findByIdDocId(docId);
    }

    /**
     * 根据标签ID查询所有文档标签关系
     * @param tagId 标签ID
     * @return 该标签下所有文档关联关系的列表
     */
    @Override
    public List<DocTags> getDocTagsByTagId(Integer tagId) {
        // 调用Repository的findByIdTId方法根据标签ID查询
        return docTagsRepository.findByIdTId(tagId);
    }

    /**
     * 查询所有文档标签关系（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的文档标签关联列表
     */
    @Override
    public Page<DocTags> getAllDocTags(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return docTagsRepository.findAll(pageable);
    }

    /**
     * 删除文档标签关系
     * @param id 复合主键对象，包含文档ID和标签ID
     */
    @Override
    public void deleteDocTag(DocTags.DocTagsId id) {
        // 调用Repository的deleteById方法根据复合主键删除
        docTagsRepository.deleteById(id);
    }

}