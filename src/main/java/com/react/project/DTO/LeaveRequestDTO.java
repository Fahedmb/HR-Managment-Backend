// File: src/main/java/com/react/project/DTO/LeaveRequestDTO.java
package com.react.project.DTO;

import com.react.project.Enumirator.LeaveStatus;
import com.react.project.Enumirator.LeaveType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String username;
    private String firstName;
    private String lastName;
    private Long approvedById;
    private String approvedByName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType type;
    private LeaveStatus status;
    private String reason;
    private String approverComment;
    private String cancellationReason;
    private Boolean halfDay;
    private Integer daysCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

