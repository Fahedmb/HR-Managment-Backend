package com.react.project.DTO;

import com.react.project.Enumirator.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSheetDTO {
    private Long id;
    private Long userId;
    private Long approvedById;
    private LocalDate date;
    private Double hoursWorked;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
