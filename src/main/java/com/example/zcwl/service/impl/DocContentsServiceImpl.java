package com.example.zcwl.service.impl;

import com.example.zcwl.entity.DocContents;
import com.example.zcwl.repository.DocContentsRepository;
import com.example.zcwl.service.DocContentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 文档内容服务实现类
 * 实现了DocContentsService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class DocContentsServiceImpl implements DocContentsService {

    private final DocContentsRepository docContentsRepository;

    /**
     * 构造函数注入
     * @param docContentsRepository 文档内容数据访问接口实例
     */
    @Autowired  // 自动注入DocContentsRepository依赖，用于数据库操作
    public DocContentsServiceImpl(DocContentsRepository docContentsRepository) {
        this.docContentsRepository = docContentsRepository;
    }

    /**
     * 创建文档内容
     * @param docContent 文档内容实体对象，包含章节信息
     * @return 创建成功的文档内容实体
     */
    @Override
    public DocContents createDocContent(DocContents docContent) {
        // 调用Repository的save方法保存文档内容实体
        return docContentsRepository.save(docContent);
    }

    /**
     * 根据复合主键查询文档内容
     * @param id 复合主键对象，包含文档ID和章节ID
     * @return 包含文档内容实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<DocContents> getDocContentById(DocContents.DocContentsId id) {
        // 调用Repository的findById方法根据复合主键查询
        return docContentsRepository.findById(id);
    }

    /**
     * 根据文档ID查询所有文档内容
     * @param docId 文档ID
     * @return 该文档下所有章节内容的列表
     */
    @Override
    public List<DocContents> getDocContentsByDocId(Integer docId) {
        // 调用Repository的findByIdDocId方法根据文档ID查询
        return docContentsRepository.findByIdDocId(docId);
    }

    /**
     * 查询所有文档内容（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的文档内容列表
     */
    @Override
    public Page<DocContents> getAllDocContents(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return docContentsRepository.findAll(pageable);
    }

    /**
     * 更新文档内容
     * @param id 复合主键对象，包含文档ID和章节ID
     * @param docContent 更新后的文档内容信息
     * @return 更新成功的文档内容实体
     * @throws RuntimeException 如果文档内容不存在
     */
    @Override
    public DocContents updateDocContent(DocContents.DocContentsId id, DocContents docContent) {
        // 先根据复合主键查询现有文档内容
        Optional<DocContents> optionalDocContent = docContentsRepository.findById(id);
        if (optionalDocContent.isPresent()) {
            // 如果存在，更新文档内容信息
            DocContents existingDocContent = optionalDocContent.get();
            existingDocContent.setName(docContent.getName());
            existingDocContent.setContent(docContent.getContent());
            existingDocContent.setDoc(docContent.getDoc());
            // 保存更新后的实体
            return docContentsRepository.save(existingDocContent);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("DocContent not found with id: " + id);
    }

    /**
     * 删除文档内容
     * @param id 复合主键对象，包含文档ID和章节ID
     */
    @Override
    public void deleteDocContent(DocContents.DocContentsId id) {
        // 调用Repository的deleteById方法根据复合主键删除
        docContentsRepository.deleteById(id);
    }

}