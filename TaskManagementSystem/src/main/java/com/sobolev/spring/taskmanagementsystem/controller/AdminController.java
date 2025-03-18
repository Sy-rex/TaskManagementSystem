package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.dto.*;
import com.sobolev.spring.taskmanagementsystem.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints for admin to manage tasks")
public class AdminController {
    private final TaskService taskService;

    @Operation(summary = "Get all tasks", description = "Retrieve all tasks in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all tasks returned successfully")
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task details returned successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer taskId) {
        TaskDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Create a new task", description = "Create a new task with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid task details")
    })
    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createTask(@RequestBody @Valid CreateTaskRequest request) {
        TaskDTO newTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @Operation(summary = "Update task", description = "Update the details of an existing task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid task details"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdateTaskRequest request) {
        TaskDTO updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Delete task", description = "Delete a task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update task status", description = "Update the status of a task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdateStatusDTO dto) {
        TaskDTO updatedTask = taskService.updateTaskStatus(taskId, dto.getStatus());
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Update task priority", description = "Update the priority of a task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task priority updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid priority value"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/tasks/{taskId}/priority")
    public ResponseEntity<TaskDTO> updateTaskPriority(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdatePriorityDTO dto) {
        TaskDTO updatedTask = taskService.updateTaskPriority(taskId, dto.getPriority());
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Assign task to user", description = "Assign a task to a specific user by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned to user successfully"),
            @ApiResponse(responseCode = "404", description = "Task or user not found")
    })
    @PostMapping("/tasks/{taskId}/assign/{userId}")
    public ResponseEntity<TaskDTO> assignTaskToUser(
            @PathVariable Integer taskId,
            @PathVariable Integer userId) {
        TaskDTO updatedTask = taskService.assignUserToTask(taskId, userId);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Add comment to task", description = "Add a comment to a task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment text"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Integer taskId,
            @RequestBody @Valid CreateCommentRequest request) {
        CommentDTO comment = taskService.addComment(taskId, request.getCommentText());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
