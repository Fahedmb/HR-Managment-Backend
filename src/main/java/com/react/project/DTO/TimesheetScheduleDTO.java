package com.react.project.DTO;

import com.react.project.Enumirator.DayOfWeekEnum;
import com.react.project.Enumirator.TimesheetStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetScheduleDTO {
    private Long id;
    private Long userId;
    private List<DayOfWeekEnum> chosenDays;
    private LocalTime startTime;
    private int totalHoursPerWeek;
    private int hoursPerDay;
    private TimesheetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userEmail;
}
