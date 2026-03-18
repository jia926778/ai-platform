package com.example.aiplatform.controller;

import com.example.aiplatform.dto.ApiResult;
import com.example.aiplatform.dto.CodeGenRequest;
import com.example.aiplatform.dto.CodeGenResponse;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.service.CodeGenService;
import com.example.aiplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/codegen")
public class CodeGenController {

    private final CodeGenService codeGenService;
    private final SysUserRepository userRepository;

    public CodeGenController(CodeGenService codeGenService, SysUserRepository userRepository) {
        this.codeGenService = codeGenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate")
    public ApiResult<CodeGenResponse> generateCode(@Valid @RequestBody CodeGenRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        CodeGenResponse response = codeGenService.generateCode(userId, request);
        return ApiResult.success(response);
    }
}
