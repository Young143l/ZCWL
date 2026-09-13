package com.example.zcwl.service;

import com.example.zcwl.entity.DocContents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 文档内容服务接口
 * 提供文档内容相关的业务逻辑操作
 */
public interface DocContentsService {

    /**
     * 创建文档内容
     * @param docContent 文档内容实体对象，包含章节的基本信息
     * @return 创建成功的文档内容实体对象
     */
    DocContents createDocContent(DocContents docContent);

    /**
     * 根据复合主键查询文档内容
     * @param id 复合主键对象，包含文档ID和章节ID
     * @return 包含文档内容实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<DocContents> getDocContentById(DocContents.DocContentsId id);

    /**
     * 根据文档ID查询所有文档内容
     * @param docId 文档ID
     * @return 该文档下所有章节内容的列表
     */
    List<DocContents> getDocContentsByDocId(Integer docId);

    /**
     * 查询所有文档内容（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含文档内容实体的分页结果
     */
    Page<DocContents> getAllDocContents(Pageable pageable);

    /**
     * 更新文档内容
     * @param id 复合主键对象，包含文档ID和章节ID
     * @param docContent 包含更新信息的文档内容实体对象
     * @return 更新后的文档内容实体对象
     */
    DocContents updateDocContent(DocContents.DocContentsId id, DocContents docContent);

    /**
     * 删除文档内容
     * @param id 复合主键对象，包含文档ID和章节ID
     */
    void deleteDocContent(DocContents.DocContentsId id);

}