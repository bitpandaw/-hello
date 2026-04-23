package com.mall.admin.file;

import com.mall.admin.security.SecurityUser;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传(本地,预留OSS)")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileUploadController {
    private final FileStorageService fileStorageService;

    @PostMapping("/api/ums/avatar")
    @Operation(summary = "用户头像")
    public Result<String> av(@RequestParam("file") MultipartFile f) {
        SecurityUser.requireMember();
        return Result.ok(fileStorageService.save(f, "avatar"));
    }

    @PostMapping("/admin/pms/file")
    @Operation(summary = "后台图片")
    public Result<String> pms(@RequestParam("file") MultipartFile f) {
        SecurityUser.requireAdmin();
        return Result.ok(fileStorageService.save(f, "pms"));
    }

    @PostMapping("/api/pms/comment/images")
    @Operation(summary = "评价图片(多张可多次调)")
    public Result<String> cmt(@RequestParam("file") MultipartFile f) {
        SecurityUser.requireMember();
        return Result.ok(fileStorageService.save(f, "comment"));
    }
}
