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

/**
 * 代码生成控制器
 * <p>
 * 提供AI代码生成的REST API接口。
 * 接收用户的功能描述，调用AI模型生成指定语言和框架的代码。
 * 需要JWT认证。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/codegen")
public class CodeGenController {

    private final CodeGenService codeGenService;
    private final SysUserRepository userRepository;

    public CodeGenController(CodeGenService codeGenService, SysUserRepository userRepository) {
        this.codeGenService = codeGenService;
        this.userRepository = userRepository;
    }

    /**
     * 根据功能描述生成代码
     *
     * @param request 代码生成请求参数（功能描述、编程语言、框架）
     * @return 代码生成响应（包含生成的代码和相关信息）
     */
    @PostMapping("/generate")
    public ApiResult<CodeGenResponse> generateCode(@Valid @RequestBody CodeGenRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        CodeGenResponse response = codeGenService.generateCode(userId, request);
        return ApiResult.success(response);
    }
}
