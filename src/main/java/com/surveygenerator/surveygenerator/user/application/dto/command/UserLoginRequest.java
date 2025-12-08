package com.surveygenerator.surveygenerator.user.application.dto.command;

public record UserLoginRequest(
        String email,
        String password
) {
}
