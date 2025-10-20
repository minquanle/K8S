// src/main/java/com/example/demo/config/DataInitializer.java
package com.example.demo.config;

import com.example.demo.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        return args -> {
            RoleEntity rAdmin = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(new RoleEntity("ROLE_ADMIN")));
            RoleEntity rUser  = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new RoleEntity("ROLE_USER")));

            if (users.findByUsername("admin").isEmpty()) {
                UserEntity u = new UserEntity();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin123"));
                u.getRoles().add(rAdmin);
                u.getRoles().add(rUser);
                users.save(u);
            }

            if (users.findByUsername("user").isEmpty()) {
                UserEntity u = new UserEntity();
                u.setUsername("user");
                u.setPassword(encoder.encode("user123"));
                u.getRoles().add(rUser);
                users.save(u);
            }
        };
    }
}
