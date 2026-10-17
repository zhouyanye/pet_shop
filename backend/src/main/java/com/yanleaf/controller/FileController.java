package com.yanleaf.controller;

import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/api/upload")
@CrossOrigin
@Tag(name = "FileController", description = "文件上传接口")

public class FileController {

    // 本地保存根目录
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 访问前缀
    @Value("${file.access-prefix}")
    private String accessPrefix;


    @Operation(summary = "测试配置", description = "调试用，返回当前 uploadDir 和 accessPrefix 配置值")
    @GetMapping("/testConfig")
    public String testConfig(){
        return "uploadDir=" + uploadDir + "<br/>accessPrefix=" + accessPrefix;
    }

    @Operation(summary = "上传图片", description = "接收 multipart/form-data 的 file 字段，仅支持 jpg/jpeg/png/webp；按日期分目录存储，返回可访问的完整 URL")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        // 校验扩展名
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".")).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|webp)")) {
            return Result.fail("只支持 jpg/jpeg/png/webp 格式");
        }

        // 按日期分目录：avatar/yyyy/mm/dd/xxx.jpg
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadDir, "avatar/" + datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("目录创建失败，路径：{}", dir.getAbsolutePath());
            return Result.fail("服务器存储目录异常，请联系管理员");
        }

        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件保存异常", e);
            return Result.fail("文件写入失败，请稍后重试");
        }

        // 返回可访问的完整 URL
        String url = accessPrefix + "/uploads/avatar/" + datePath + "/" + fileName;
        return Result.ok(url);
    }
}