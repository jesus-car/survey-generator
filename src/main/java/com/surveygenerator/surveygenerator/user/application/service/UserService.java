package com.surveygenerator.surveygenerator.user.application.service;

import com.surveygenerator.surveygenerator.user.application.dto.command.UserLoginRequest;
import com.surveygenerator.surveygenerator.user.application.dto.command.UserRegisterRequest;
import com.surveygenerator.surveygenerator.user.application.dto.response.UserLoginResponse;
import com.surveygenerator.surveygenerator.user.application.dto.response.UserRegisterResponse;
import com.surveygenerator.surveygenerator.user.application.mapper.UserMapper;
import com.surveygenerator.surveygenerator.user.application.port.output.UserAccessDatabasePort;
import com.surveygenerator.surveygenerator.user.domain.model.UserModel;
import com.surveygenerator.surveygenerator.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccessDatabasePort userAccessDatabasePort;
    private final UserMapper userMapper;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    public UserLoginResponse login(UserLoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserModel user = userAccessDatabasePort.findByUsername(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String accessToken = jwtUtils.generateAccessToken(user);

        return new UserLoginResponse(accessToken);
    }

    public UserRegisterResponse registerUser(UserRegisterRequest request) {

        if(userAccessDatabasePort.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if(userAccessDatabasePort.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        return userMapper.toUserRegisterResponse(
                userAccessDatabasePort.save(userMapper.toUserModel(request))
        );
    }
}
