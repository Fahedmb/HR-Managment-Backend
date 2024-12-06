package com.react.project.Mapper;

import com.react.project.DTO.TimesheetScheduleDTO;
import com.react.project.Model.TimesheetSchedule;
import com.react.project.Model.User;

public class TimesheetScheduleMapper {
    public static TimesheetScheduleDTO toDTO(TimesheetSchedule schedule) {
        return TimesheetScheduleDTO.builder()
                .id(schedule.getId())
                .userId(schedule.getUser().getId())
                .chosenDays(schedule.getChosenDays())
                .startTime(schedule.getStartTime())
                .totalHoursPerWeek(schedule.getTotalHoursPerWeek())
                .hoursPerDay(schedule.getHoursPerDay())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    public static TimesheetSchedule toEntity(TimesheetScheduleDTO dto, User user) {
        return TimesheetSchedule.builder()
                .id(dto.getId())
                .user(user)
                .chosenDays(dto.getChosenDays())
                .startTime(dto.getStartTime())
                .totalHoursPerWeek(dto.getTotalHoursPerWeek())
                .hoursPerDay(dto.getHoursPerDay())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
