package com.react.project.Mapper;

import com.react.project.Model.LeaveRequest;
import com.react.project.Model.User;
import com.react.project.DTO.LeaveRequestDTO;

public class LeaveRequestMapper {

    public static LeaveRequestDTO toDTO(LeaveRequest leaveRequest) {
        return LeaveRequestDTO.builder()
                .id(leaveRequest.getId())
                .userId(leaveRequest.getUser().getId())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .type(leaveRequest.getType())
                .status(leaveRequest.getStatus())
                .reason(leaveRequest.getReason())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    public static LeaveRequest toEntity(LeaveRequestDTO dto, User user) {
        return LeaveRequest.builder()
                .id(dto.getId())
                .user(user)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .type(dto.getType())
                .status(dto.getStatus())
                .reason(dto.getReason())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
