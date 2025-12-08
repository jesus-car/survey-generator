package com.surveygenerator.surveygenerator.user.application.port.output;

import com.surveygenerator.surveygenerator.user.domain.model.UserModel;

import java.util.Optional;

public interface UserAccessDatabasePort {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    UserModel save(UserModel userModel);
    Optional<UserModel> findByUsername(String username);
}
