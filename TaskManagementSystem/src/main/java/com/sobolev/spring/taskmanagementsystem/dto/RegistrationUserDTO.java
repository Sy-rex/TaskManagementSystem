package com.sobolev.spring.taskmanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationUserDTO {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 255, message = "Имя пользователя должно быть от 3 до 255 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 5, max = 255, message = "Пароль должен быть от 5 до 255 символов")
    private String password;

    @NotBlank(message = "Поле email не может быть пустым")
    @Email(message = "Неправильный формат email")
    private String email;
}