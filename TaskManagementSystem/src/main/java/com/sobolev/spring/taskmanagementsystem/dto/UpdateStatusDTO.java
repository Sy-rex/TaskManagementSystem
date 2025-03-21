package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusDTO {
    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Неправильное значение")
    @NotNull(message = "Статус не должен быть пустым")
    private String status;
}
