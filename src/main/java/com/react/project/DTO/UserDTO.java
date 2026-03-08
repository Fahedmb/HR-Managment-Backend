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
    /** Number of leave days used in the current year (tracked on the User entity). */
    private int usedDaysThisYear;
    /** Remaining leave balance = 30 - usedDaysThisYear. */
    private int leaveBalance;
}
