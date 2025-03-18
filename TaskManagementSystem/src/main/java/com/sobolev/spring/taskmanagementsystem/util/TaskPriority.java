package com.sobolev.spring.taskmanagementsystem.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskPriority {
    HIGH("HIGH", "Высокий"),
    MEDIUM("MEDIUM", "Средний"),
    LOW("LOW", "Низкий");

    private final String dbValue;
    private final String displayName;
}
