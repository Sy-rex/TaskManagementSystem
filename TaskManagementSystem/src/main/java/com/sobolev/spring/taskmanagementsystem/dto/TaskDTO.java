package com.sobolev.spring.taskmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO{
    private String title;
    private String description;
    private String status;
    private String priority;
    private String authorName;
    private List<String> assignees;
    private List<CommentDTO> comments;
}
