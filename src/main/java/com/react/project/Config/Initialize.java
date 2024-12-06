package com.react.project.Config;

import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.DTO.UserDTO;
import com.react.project.Enumirator.Role;
import com.react.project.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class Initialize implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        try {
            createAdminUserIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    private void createAdminUserIfNeeded() {
        try {

            RegisterRequest adminRequest = new RegisterRequest();
            adminRequest.setEmail("admin@admin.com");

            userService.getUserByEmail(adminRequest.getEmail());

        } catch (Exception e) {

            RegisterRequest adminRequest = RegisterRequest.builder()
                    .firstName("Admin")
                    .lastName("Admin")
                    .email("admin@admin.com")
                    .username("ADMIN")
                    .password("admin123")
                    .role(Role.HR)
                    .build();

            RegisterResponse response = userService.register(adminRequest);

            System.out.println("Admin user created: " + response.getEmailResponse());
        }
    }
}
