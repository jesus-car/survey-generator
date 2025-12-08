package com.surveygenerator.surveygenerator.user.application.dto.command;


public record UserRegisterRequest (
    String username,
    String email,
    String password
) {}
