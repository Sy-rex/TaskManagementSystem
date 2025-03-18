package com.sobolev.spring.taskmanagementsystem.dto;

import com.sobolev.spring.taskmanagementsystem.util.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusDTO {
    @NotNull(message = "Статус не должен быть пустым")
    private TaskStatus status;
}
