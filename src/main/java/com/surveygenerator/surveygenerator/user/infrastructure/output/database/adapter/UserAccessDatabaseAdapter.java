package com.surveygenerator.surveygenerator.user.infrastructure.output.database.adapter;

import com.surveygenerator.surveygenerator.user.application.port.output.UserAccessDatabasePort;
import com.surveygenerator.surveygenerator.user.domain.model.UserModel;
import com.surveygenerator.surveygenerator.user.infrastructure.output.database.entity.UserEntity;
import com.surveygenerator.surveygenerator.user.infrastructure.output.database.mapper.UserEntityMapper;
import com.surveygenerator.surveygenerator.user.infrastructure.output.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAccessDatabaseAdapter implements UserAccessDatabasePort {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserModel save(UserModel userModel) {
        UserEntity entityToSave = userEntityMapper.toEntity(userModel);
        UserEntity savedEntity = userRepository.save(entityToSave);
        return userEntityMapper.toModel(savedEntity);
    }

    @Override
    public Optional<UserModel> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userEntityMapper::toModel);
    }

}
