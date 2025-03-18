package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

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
