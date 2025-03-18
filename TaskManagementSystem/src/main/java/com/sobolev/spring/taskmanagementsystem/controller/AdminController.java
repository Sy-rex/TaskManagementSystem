package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.dto.CreateTaskRequest;
import com.sobolev.spring.taskmanagementsystem.dto.TaskDTO;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.service.TaskService;
import com.sobolev.spring.taskmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

//    @GetMapping("/tasks/author")
//    public List<TaskDTO> getTasksByAuthor(HttpServletRequest request){
//        String token = jwtTokenUtils.extractToken(request);
//        Optional<User> user = userService.findByUsername(jwtTokenUtils.getUsernameFromToken(token));
//
//        if (user.isPresent()) {
//            taskService.findAllTasksByAuthor(user);
//        }
//    }
}
