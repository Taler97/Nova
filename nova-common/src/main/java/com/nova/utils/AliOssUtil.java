package com.nova.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class AliOssUtil {
    private OSS ossClient;
    private String bucketName;
    private String endpoint;
    private long expirationSeconds;

    public AliOssUtil(String endpoint, String accessKeyId, String accessKeySecret, String bucketName, long expirationSeconds) {
        this.bucketName = bucketName;
        this.endpoint = endpoint;
        this.expirationSeconds = expirationSeconds;
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public String upload(byte[] bytes, String originalFilename, String category) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = category + "/" + UUID.randomUUID() + ext;
        ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        return "https://" + bucketName + "." + endpoint.replaceAll("https?://", "") + "/" + objectName;
    }

    public String generateSignedUrl(String objectKey) {
        Date expiration = new Date(System.currentTimeMillis() + expirationSeconds * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
        request.setExpiration(expiration);
        URL signedUrl = ossClient.generatePresignedUrl(request);
        return signedUrl.toString();
    }

    public String extractObjectKey(String ossUrl) {
        if (ossUrl == null || ossUrl.isBlank()) return null;
        String cleanUrl = ossUrl.replaceAll("https?://", "");
        int slashIndex = cleanUrl.indexOf("/");
        if (slashIndex == -1) return null;
        return cleanUrl.substring(slashIndex + 1);
    }

    public String convertToSignedUrl(String ossUrl) {
        String objectKey = extractObjectKey(ossUrl);
        if (objectKey == null) return ossUrl;
        return generateSignedUrl(objectKey);
    }

    public void close() {
        if (ossClient != null) ossClient.shutdown();
    }
}
