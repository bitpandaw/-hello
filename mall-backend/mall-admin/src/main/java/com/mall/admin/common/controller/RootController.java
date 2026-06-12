package com.mall.admin.common.controller;

import com.mall.common.api.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Result<Map<String, String>> index() {
        return Result.ok(Map.of(
                "name", "mall-backend",
                "status", "ok",
                "apiDocs", "/doc.html",
                "openapi", "/v3/api-docs"
        ));
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
