package com.example.zcwl.utils;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

/**
 * 七牛云工具类
 */
@Component
public class QiniuUtil {

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
        String baseUrl = domain + "/" + fileName;
        return auth.privateDownloadUrl(baseUrl, expires);
    }

    /**
     * 生成公开文件的访问URL
     * @param fileName 文件名
     * @return 公开访问URL
     */
    public String getPublicUrl(String fileName) {
        return domain + "/" + fileName;
    }
}
