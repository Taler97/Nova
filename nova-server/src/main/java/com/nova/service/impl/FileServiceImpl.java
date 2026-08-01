package com.nova.service.impl;

import com.nova.constant.OssDirectory;
import com.nova.service.FileService;
import com.nova.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AliOssUtil aliOssUtil;

    @Override
    public Map<String, String> upload(MultipartFile file) {
        return upload(file, OssDirectory.COMMON);
    }

    @Override
    public Map<String, String> upload(MultipartFile file, String category) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newName = UUID.randomUUID() + ext;
            String ossUrl = aliOssUtil.upload(file.getBytes(), newName, category);
            String signedUrl = aliOssUtil.convertToSignedUrl(ossUrl);
            Map<String, String> result = new HashMap<>();
            result.put("ossUrl", ossUrl);
            result.put("signedUrl", signedUrl);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
