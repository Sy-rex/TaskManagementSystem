package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.dto.CommentDTO;
import com.sobolev.spring.taskmanagementsystem.dto.CreateCommentRequest;
import com.sobolev.spring.taskmanagementsystem.dto.TaskDTO;
import com.sobolev.spring.taskmanagementsystem.dto.UpdateStatusDTO;
import com.sobolev.spring.taskmanagementsystem.model.Task;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.security.JwtTokenUtils;
import com.sobolev.spring.taskmanagementsystem.service.TaskService;
import com.sobolev.spring.taskmanagementsystem.service.UserService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final TaskService taskService;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;


    @GetMapping("/tasks/assignee")
    public ResponseEntity<List<TaskDTO>> getTasksForAssignee(HttpServletRequest request) {
        String token = jwtTokenUtils.extractToken(request);
        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));

        if (user.isPresent()) {
            System.out.println("Запрошены задачи для пользователя с ID: {}" + user.get().getId());
            List<TaskDTO> tasks = taskService.getTasksByAssignee(user.get());
            return ResponseEntity.ok(tasks);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("tasks/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer taskId, HttpServletRequest request) {
        String token = jwtTokenUtils.extractToken(request);
        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TaskDTO task = taskService.getTaskWithPermissionsCheck(taskId, user.get());
        return ResponseEntity.ok(task);
    }

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

        System.out.println("Пользователь " + user.get().getUsername() + " обновляет статус задачи " + taskId + " на " + dto.getStatus());

        TaskDTO updatedTask = taskService.updateTaskStatus(taskId, dto.getStatus(), user.get());
        return ResponseEntity.ok(updatedTask);
    }


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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        System.out.println("Пользователь " + user.get().getUsername() +
                " добавляет комментарий к задаче " + taskId + ": " + request.getCommentText());

        CommentDTO comment = taskService.addComment(taskId, request.getCommentText(), user.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }


}
