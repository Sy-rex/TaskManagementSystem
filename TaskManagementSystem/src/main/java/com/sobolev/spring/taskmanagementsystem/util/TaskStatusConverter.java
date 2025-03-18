package com.sobolev.spring.taskmanagementsystem.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {
    @Override
    public String convertToDatabaseColumn(TaskStatus status) {
        return status.getDbValue();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String dbValue) {
        return Arrays.stream(TaskStatus.values())
                .filter(s -> s.getDbValue().equals(dbValue))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}