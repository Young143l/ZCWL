package com.example.zcwl.utils;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 七牛云工具类
 */
@Component
public class QiniuUtil {

    private static final Logger logger = LoggerFactory.getLogger(QiniuUtil.class);

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket-name}")
    private String bucketName;

    @Value("${qiniu.domain}")
    private String domain;

    @Value("${qiniu.region}")
    private String region;

    /**
     * 生成上传凭证
     * @return 上传凭证
     */
    public String getUploadToken() {
        Auth auth = Auth.create(accessKey, secretKey);
        return auth.uploadToken(bucketName);
    }

    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @return 文件访问URL
     */
    public String uploadFile(InputStream inputStream, String fileName) {
        // 配置七牛云存储区域
        Configuration cfg = new Configuration(getRegion());
        UploadManager uploadManager = new UploadManager(cfg);
        
        try {
            // 上传文件
            uploadManager.put(inputStream, fileName, getUploadToken(), null, null);
            // 返回文件访问URL
            return domain + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 根据配置获取七牛云存储区域
     * @return 存储区域
     */
    private Region getRegion() {
        switch (region) {
            case "z0":
                return Region.region0(); // 华东
            case "z1":
                return Region.region1(); // 华北
            case "z2":
                return Region.region2(); // 华南
            case "na0":
                return Region.regionNa0(); // 北美
            case "as0":
                return Region.regionAs0(); // 亚太
            default:
                return Region.region0(); // 默认华东
        }
    }

    /**
     * 生成文件下载URL
     * @param fileName 文件名
     * @param expires 过期时间（秒）
     * @return 带签名的下载URL
     */
    public String getDownloadUrl(String fileName, long expires) {
        Auth auth = Auth.create(accessKey, secretKey);
        
        // 确保domain包含协议头
        String baseUrl = domain;
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }
        
        // 构建完整URL
        baseUrl = baseUrl + "/" + fileName;
        
        String downloadUrl = auth.privateDownloadUrl(baseUrl, expires);
        
        logger.debug("生成下载URL - baseUrl: {}, expires: {}秒, downloadUrl: {}", baseUrl, expires, downloadUrl);
        
        return downloadUrl;
    }

    /**
     * 生成公开文件的访问URL
     * @param fileName 文件名
     * @return 公开访问URL
     */
    public String getPublicUrl(String fileName) {
        return domain + "/" + fileName;
    }

    /**
     * 列出指定前缀的文件列表
     * @param prefix 文件前缀（项目路径）
     * @return 文件信息列表
     */
    public List<FileInfo> listFiles(String prefix) {
        return listFiles(prefix, 1000);
    }

    /**
     * 列出指定前缀的文件列表
     * @param prefix 文件前缀（项目路径）
     * @param limit 每次查询的最大数量
     * @return 文件信息列表
     */
    public List<FileInfo> listFiles(String prefix, int limit) {
        List<FileInfo> allFiles = new ArrayList<>();
        Configuration cfg = new Configuration(getRegion());
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        
        String marker = null;
        try {
            do {
                FileListing fileListing = bucketManager.listFiles(bucketName, prefix, marker, limit, null);
                if (fileListing.items != null) {
                    for (FileInfo fileInfo : fileListing.items) {
                        allFiles.add(fileInfo);
                    }
                }
                marker = fileListing.marker;
            } while (marker != null && !marker.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException("获取文件列表失败", e);
        }
        
        return allFiles;
    }

    /**
     * 删除单个文件
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) {
        Configuration cfg = new Configuration(getRegion());
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        
        try {
            bucketManager.delete(bucketName, fileName);
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败", e);
        }
    }

    /**
     * 批量删除文件
     * @param fileNames 文件名列表
     */
    public void deleteFiles(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }
        
        Configuration cfg = new Configuration(getRegion());
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        
        try {
            for (String fileName : fileNames) {
                try {
                    bucketManager.delete(bucketName, fileName);
                } catch (Exception e) {
                    // 记录单个文件删除失败，但继续删除其他文件
                    logger.error("删除文件失败: {}", fileName, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("批量删除文件失败", e);
        }
    }

    /**
     * 删除指定前缀的所有文件（用于删除整个项目）
     * @param prefix 文件前缀（项目路径）
     */
    public void deleteFilesByPrefix(String prefix) {
        List<FileInfo> files = listFiles(prefix);
        List<String> fileNames = new ArrayList<>();
        
        for (FileInfo fileInfo : files) {
            fileNames.add(fileInfo.key);
        }
        
        deleteFiles(fileNames);
    }

    /**
     * 检查文件是否存在
     * @param fileName 文件名
     * @return 是否存在
     */
    public boolean fileExists(String fileName) {
        try {
            List<FileInfo> files = listFiles(fileName);
            for (FileInfo fileInfo : files) {
                if (fileInfo.key.equals(fileName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("检查文件是否存在失败: {}", fileName, e);
            return false;
        }
    }
}
