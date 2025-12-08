package com.surveygenerator.surveygenerator.user.infrastructure.input.controller;

import com.surveygenerator.surveygenerator.user.application.dto.command.UserLoginRequest;
import com.surveygenerator.surveygenerator.user.application.dto.command.UserRegisterRequest;
import com.surveygenerator.surveygenerator.user.application.dto.response.UserLoginResponse;
import com.surveygenerator.surveygenerator.user.application.dto.response.UserRegisterResponse;
import com.surveygenerator.surveygenerator.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(
            @RequestBody UserRegisterRequest request
    ) {
        log.info("Registering user with email: {}", request.email());
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(
            @RequestBody UserLoginRequest request
    ) {
        log.info("User login attempt with email: {}", request.email());
        return ResponseEntity.ok(userService.login(request));
    }
}
