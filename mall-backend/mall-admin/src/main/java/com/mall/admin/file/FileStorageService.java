package com.mall.admin.file;

import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${mall.file.upload-dir:./mall-uploads}")
    private String uploadDir;

    public String save(MultipartFile f, String sub) {
        if (f == null || f.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "空文件");
        }
        String ext = "";
        String on = f.getOriginalFilename();
        if (on != null && on.contains(".")) {
            ext = on.substring(on.lastIndexOf('.'));
        }
        String name = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, sub, LocalDate.now().toString());
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(name);
            f.transferTo(target.toFile());
            return "/uploads/" + sub + "/" + LocalDate.now() + "/" + name;
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BUSINESS, "文件保存失败");
        }
    }
}
