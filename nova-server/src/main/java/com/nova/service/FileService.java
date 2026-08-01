package com.nova.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface FileService {
    Map<String, String> upload(MultipartFile file);

    Map<String, String> upload(MultipartFile file, String category);
}
