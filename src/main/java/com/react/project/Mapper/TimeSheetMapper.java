package com.react.project.Mapper;

import com.react.project.Model.TimeSheet;
import com.react.project.DTO.TimeSheetDTO;
import com.react.project.Model.User;

public class TimeSheetMapper {

    public static TimeSheetDTO toDTO(TimeSheet timeSheet) {
        return TimeSheetDTO.builder()
                .id(timeSheet.getId())
                .userId(timeSheet.getUser().getId())
                .approvedById(
                        timeSheet.getApprovedBy() != null ? timeSheet.getApprovedBy().getId() : null
                )
                .date(timeSheet.getDate())
                .hoursWorked(timeSheet.getHoursWorked())
                .status(timeSheet.getStatus())
                .createdAt(timeSheet.getCreatedAt())
                .updatedAt(timeSheet.getUpdatedAt())
                .build();
    }

    public static TimeSheet toEntity(TimeSheetDTO dto, User user, User approvedBy) {
        return TimeSheet.builder()
                .id(dto.getId())
                .user(user)
                .approvedBy(approvedBy)
                .date(dto.getDate())
                .hoursWorked(dto.getHoursWorked())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
