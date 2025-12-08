package com.surveygenerator.surveygenerator.user.application.mapper;

import com.surveygenerator.surveygenerator.user.application.dto.command.UserRegisterRequest;
import com.surveygenerator.surveygenerator.user.application.dto.response.UserRegisterResponse;
import com.surveygenerator.surveygenerator.user.domain.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserModel toUserModel(UserRegisterRequest request) {
        return UserModel.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of("USER"))
                .build();
    }

    public UserRegisterResponse toUserRegisterResponse(UserModel userModel) {
        return new UserRegisterResponse(
                userModel.getUsername(),
                userModel.getEmail()
        );
    }
}
