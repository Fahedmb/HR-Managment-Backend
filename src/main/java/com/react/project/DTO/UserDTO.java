package com.react.project.DTO;

import com.react.project.Enumirator.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String position;
    private String department;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
