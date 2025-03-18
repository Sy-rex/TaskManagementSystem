package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Неправильное значение")
    @NotBlank(message = "Статус не должен быть пустым")
    private String status;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Неправильное значение")
    @NotBlank(message = "Приоритет не должен быть пустым")
    private String priority;

    private List<String> assigneeNames;
}
