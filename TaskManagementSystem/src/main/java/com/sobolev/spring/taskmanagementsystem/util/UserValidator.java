package com.sobolev.spring.taskmanagementsystem.util;

import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {
    private final UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        if (userService.findByUsername(user.getUsername()).isPresent()){
            errors.rejectValue("username", "", "Пользователь с таким именем уже существует");
        }

        if (userService.findByEmail(user.getEmail()).isPresent()){
            errors.rejectValue("email","","Такой email уже используется другим пользователем");
        }
    }
}
