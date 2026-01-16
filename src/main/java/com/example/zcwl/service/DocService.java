package com.example.zcwl.service;

import com.example.zcwl.entity.Doc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 文档服务接口
 * 提供文档相关的业务逻辑操作
 */
public interface DocService {

    /**
     * 创建文档
     * @param doc 文档实体对象，包含文档的基本信息
     * @return 创建成功的文档实体对象
     */
    Doc createDoc(Doc doc);

    /**
     * 根据ID查询文档
     * @param id 文档ID
     * @return 包含文档实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<Doc> getDocById(Integer id);

    /**
     * 查询所有文档（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含文档实体的分页结果
     */
    Page<Doc> getAllDocs(Pageable pageable);

    /**
     * 更新文档
     * @param id 文档ID
     * @param doc 包含更新信息的文档实体对象
     * @return 更新后的文档实体对象
     */
    Doc updateDoc(Integer id, Doc doc);

    /**
     * 删除文档
     * @param id 文档ID
     */
    void deleteDoc(Integer id);

    /**
     * 根据文档名称查询文档
     * @param docName 文档名称
     * @return 匹配的文档实体对象，如果未找到则返回null
     */
    Doc getDocByDocName(String docName);

}