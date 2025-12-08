package com.surveygenerator.surveygenerator.user.infrastructure.config;

import com.surveygenerator.surveygenerator.user.application.port.output.UserAccessDatabasePort;
import com.surveygenerator.surveygenerator.user.domain.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class UserDetailsServiceConfig {

    private final UserAccessDatabasePort userAccessDatabasePort;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserModel user = userAccessDatabasePort.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return new CustomUserDetails(
                    user.getId(),  // ← Incluir userId
                    user.getUsername(),
                    user.getPassword(),
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toSet()),
                    user.isActive()
            );
        };
    }
}
