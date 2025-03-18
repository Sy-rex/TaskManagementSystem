package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Заголовок не должен быть пустым")
    private String title;

    @NotBlank(message = "Описание не должно быть пустым")
    private String description;

    @NotBlank(message = "Статус не должен быть пустым")
    private String status; // Например, "OPEN", "IN_PROGRESS", "DONE"

    @NotBlank(message = "Приоритет не должен быть пустым")
    private String priority; // Например, "LOW", "MEDIUM", "HIGH"

    private List<String> assigneeNames;
}
