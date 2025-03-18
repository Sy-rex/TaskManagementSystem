package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private Integer id;

    @NotBlank(message = "Комментарий не может быть пустым")
    private String commentText;

    private String authorName;

    private Date createdAt;
}
