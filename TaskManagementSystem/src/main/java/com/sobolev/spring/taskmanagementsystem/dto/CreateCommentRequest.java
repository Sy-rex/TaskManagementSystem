package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Комментарий не должен быть пустым")
    @Size(max = 1000, message = "Комментарий не должен быть более 1000 символов")
    private String commentText;
}
