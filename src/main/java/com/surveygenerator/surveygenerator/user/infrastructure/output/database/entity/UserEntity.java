package com.surveygenerator.surveygenerator.user.infrastructure.output.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    // Cuando construimos un usuario, por defecto tendrá el rol "USER"
    // Sin esta anotacion el valor por defecto no se asigna al usar el builder
    @Builder.Default
    private Set<RoleEntity> roles = Set.of(new RoleEntity("USER"));

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
}
