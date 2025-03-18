package com.sobolev.spring.taskmanagementsystem.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    PENDING("PENDING", "В ожидании"),
    IN_PROGRESS("IN_PROGRESS", "В процессе"),
    COMPLETED("COMPLETED", "Завершено");

    private final String dbValue;
    private final String displayName;
}
