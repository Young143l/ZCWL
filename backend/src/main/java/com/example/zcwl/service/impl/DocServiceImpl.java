package com.example.zcwl.service.impl;

import com.example.zcwl.entity.Doc;
import com.example.zcwl.repository.DocRepository;
import com.example.zcwl.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 文档服务实现类
 * 实现了DocService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class DocServiceImpl implements DocService {

    private final DocRepository docRepository;

    /**
     * 构造函数注入
     * @param docRepository 文档数据访问接口实例
     */
    @Autowired  // 自动注入DocRepository依赖，用于数据库操作
    public DocServiceImpl(DocRepository docRepository) {
        this.docRepository = docRepository;
    }

    /**
     * 创建文档
     * @param doc 文档实体对象，包含文档基本信息
     * @return 创建成功的文档实体
     */
    @Override
    public Doc createDoc(Doc doc) {
        // 调用Repository的save方法保存文档实体
        return docRepository.save(doc);
    }

    /**
     * 根据ID查询文档
     * @param id 文档ID
     * @return 包含文档实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<Doc> getDocById(Integer id) {
        // 调用Repository的findById方法根据ID查询
        return docRepository.findById(id);
    }

    /**
     * 查询所有文档（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的文档列表
     */
    @Override
    public Page<Doc> getAllDocs(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return docRepository.findAll(pageable);
    }

    /**
     * 更新文档
     * @param id 文档ID
     * @param doc 更新后的文档信息
     * @return 更新成功的文档实体
     * @throws RuntimeException 如果文档不存在
     */
    @Override
    public Doc updateDoc(Integer id, Doc doc) {
        // 先根据ID查询现有文档
        Optional<Doc> optionalDoc = docRepository.findById(id);
        if (optionalDoc.isPresent()) {
            // 如果存在，更新文档信息
            Doc existingDoc = optionalDoc.get();
            existingDoc.setDocName(doc.getDocName());
            existingDoc.setSummary(doc.getSummary());
            existingDoc.setIcon(doc.getIcon());
            // 保存更新后的实体
            return docRepository.save(existingDoc);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("Doc not found with id: " + id);
    }

    /**
     * 删除文档
     * @param id 文档ID
     */
    @Override
    public void deleteDoc(Integer id) {
        // 调用Repository的deleteById方法根据ID删除
        docRepository.deleteById(id);
    }

    /**
     * 根据文档名称查询文档
     * @param docName 文档名称
     * @return 匹配的文档实体，如果不存在则返回null
     */
    @Override
    public Doc getDocByDocName(String docName) {
        // 调用Repository的findByDocName方法根据名称查询
        return docRepository.findByDocName(docName);
    }

    /**
     * 查询所有文档
     * @return 文档实体列表
     */
    @Override
    public java.util.List<Doc> getAllDocs() {
        // 调用Repository的findAll方法获取所有文档
        return docRepository.findAll();
    }

}