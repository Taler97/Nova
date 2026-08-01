package com.nova.controller.admin;

import com.nova.result.Result;
import com.nova.service.FileService;
import com.nova.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/admin/file")
@Slf4j
@Tag(name = "文件上传相关接口")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<Map<String, String>> upload(MultipartFile file,
            @RequestParam(defaultValue = "common") String category) {
        Map<String, String> result = fileService.upload(file, category);
        return Result.success(result);
    }

    @GetMapping("/signed-url")
    @Operation(summary = "获取图片签名URL")
    public Result<String> getSignedUrl(@RequestParam String ossUrl) {
        return Result.success(aliOssUtil.convertToSignedUrl(ossUrl));
    }
}
