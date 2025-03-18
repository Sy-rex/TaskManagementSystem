package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriorityDTO {
    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Неправильное значение")
    @NotNull(message = "Приоритет не должен быть пустым")
    private String priority;
}
