package com.example.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @Builder.Default
    private String language = "Java";

    private String framework;
}
