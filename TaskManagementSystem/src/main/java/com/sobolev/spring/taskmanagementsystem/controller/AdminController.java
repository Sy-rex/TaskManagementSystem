package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.dto.*;
import com.sobolev.spring.taskmanagementsystem.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TaskService taskService;

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        System.out.println("admin work");
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer taskId) {
        TaskDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createTask(@RequestBody @Valid CreateTaskRequest request) {
        TaskDTO newTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdateTaskRequest request) {
        TaskDTO updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdateStatusDTO dto) {
        TaskDTO updatedTask = taskService.updateTaskStatus(taskId, dto.getStatus());
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/tasks/{taskId}/priority")
    public ResponseEntity<TaskDTO> updateTaskPriority(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdatePriorityDTO dto) {
        TaskDTO updatedTask = taskService.updateTaskPriority(taskId, dto.getPriority());
        return ResponseEntity.ok(updatedTask);
    }

    @PostMapping("/tasks/{taskId}/assign/{userId}")
    public ResponseEntity<TaskDTO> assignTaskToUser(
            @PathVariable Integer taskId,
            @PathVariable Integer userId) {
        TaskDTO updatedTask = taskService.assignUserToTask(taskId, userId);
        return ResponseEntity.ok(updatedTask);
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Integer taskId,
            @RequestBody @Valid CreateCommentRequest request) {
        CommentDTO comment = taskService.addComment(taskId, request.getCommentText());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
