package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.dto.CommentDTO;
import com.sobolev.spring.taskmanagementsystem.dto.CreateCommentRequest;
import com.sobolev.spring.taskmanagementsystem.dto.TaskDTO;
import com.sobolev.spring.taskmanagementsystem.dto.UpdateStatusDTO;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.security.JwtTokenUtils;
import com.sobolev.spring.taskmanagementsystem.service.TaskService;
import com.sobolev.spring.taskmanagementsystem.service.UserService;
import com.sobolev.spring.taskmanagementsystem.util.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for users to manage tasks and comments")
public class UserController {
    private final TaskService taskService;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;

    @Operation(summary = "Get tasks for current user", description = "Retrieve all tasks assigned to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/tasks/assignee")
    public ResponseEntity<List<TaskDTO>> getTasksForAssignee(HttpServletRequest request) {
        String token = jwtTokenUtils.extractToken(request);
        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));

        if (user.isPresent()) {
            List<TaskDTO> tasks = taskService.getTasksByAssignee(user.get());
            return ResponseEntity.ok(tasks);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID if the user has permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task details returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Task not found or no permission")
    })
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer taskId, HttpServletRequest request) {
        String token = jwtTokenUtils.extractToken(request);
        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TaskDTO task = taskService.getTaskWithPermissionsCheck(taskId, user.get());
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Update task status", description = "Update the status of a task by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Task not found or no permission")
    })
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Integer taskId,
            @RequestBody @Valid UpdateStatusDTO dto,
            HttpServletRequest request) {

        String token = jwtTokenUtils.extractToken(request);
        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        TaskDTO updatedTask = taskService.updateTaskStatus(taskId, TaskStatus.valueOf(dto.getStatus()), user.get());
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Add comment to task", description = "Add a comment to a specific task by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment text"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Task not found or no permission")
    })
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Integer taskId,
            @RequestBody @Valid CreateCommentRequest request,
            HttpServletRequest httpRequest) {

        String token = jwtTokenUtils.extractToken(httpRequest);
        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (request.getCommentText() == null || request.getCommentText().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        CommentDTO comment = taskService.addComment(taskId, request.getCommentText(), user.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
