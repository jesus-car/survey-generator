package com.surveygenerator.surveygenerator.user.infrastructure.output.database.mapper;

import com.surveygenerator.surveygenerator.user.domain.model.UserModel;
import com.surveygenerator.surveygenerator.user.infrastructure.output.database.entity.RoleEntity;
import com.surveygenerator.surveygenerator.user.infrastructure.output.database.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserEntityMapper {

    public UserModel toModel(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserModel.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .roles(entity.getRoles() != null ?
                    entity.getRoles().stream()
                        .map(RoleEntity::getRole)
                        .collect(Collectors.toSet()) : Set.of())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .active(entity.isActive())
                .build();
    }

    public UserEntity toEntity(UserModel model) {
        if (model == null) {
            return null;
        }

        return UserEntity.builder()
                .id(model.getId())
                .username(model.getUsername())
                .email(model.getEmail())
                .password(model.getPassword())
                .roles(model.getRoles() != null ?
                    model.getRoles().stream()
                        .map(roleName -> RoleEntity.builder()
                            .role(roleName)
                            .build())
                        .collect(Collectors.toSet()) : Set.of(new RoleEntity("USER")))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}