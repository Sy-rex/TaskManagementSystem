package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private String title;
    private String description;

    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Неправильное значение")
    private String status;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Неправильное значение")
    private String priority;

    private List<String> assigneeNames;
}
