package com.sobolev.spring.taskmanagementsystem.controller;

import com.sobolev.spring.taskmanagementsystem.dto.JwtRequest;
import com.sobolev.spring.taskmanagementsystem.dto.JwtResponse;
import com.sobolev.spring.taskmanagementsystem.dto.RegistrationUserDTO;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.security.JwtTokenUtils;
import com.sobolev.spring.taskmanagementsystem.service.RegistrationService;
import com.sobolev.spring.taskmanagementsystem.util.UserValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final RegistrationService registrationService;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/registration")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationUserDTO registrationUserDTO,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",bindingResult.getAllErrors()));
        }

        User user = convertToUser(registrationUserDTO);
        userValidator.validate(user, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",bindingResult.getFieldError().getDefaultMessage()));
        }

        registrationService.register(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest authRequest){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                            authRequest.getPassword()));
        }catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message","Неправильный логин или пароль"));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    };


    private User convertToUser(RegistrationUserDTO registrationUserDTO) {
        return modelMapper.map(registrationUserDTO, User.class);
    }
}
