package com.sobolev.spring.taskmanagementsystem.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class TaskPriorityConverter implements AttributeConverter<TaskPriority, String> {
    @Override
    public String convertToDatabaseColumn(TaskPriority priority) {
        return priority.getDbValue();
    }

    @Override
    public TaskPriority convertToEntityAttribute(String dbValue) {
        return Arrays.stream(TaskPriority.values())
                .filter(s -> s.getDbValue().equals(dbValue))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
