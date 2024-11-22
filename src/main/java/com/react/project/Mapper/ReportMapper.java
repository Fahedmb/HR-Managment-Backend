package com.react.project.Mapper;

import com.react.project.Model.Report;
import com.react.project.DTO.ReportDTO;
import com.react.project.Model.User;

public class ReportMapper {

    public static ReportDTO toDTO(Report report) {
        return ReportDTO.builder()
                .id(report.getId())
                .generatedById(report.getGeneratedBy().getId())
                .type(report.getType())
                .data(report.getData())
                .generatedAt(report.getGeneratedAt())
                .build();
    }

    public static Report toEntity(ReportDTO dto, User generatedBy) {
        return Report.builder()
                .id(dto.getId())
                .generatedBy(generatedBy)
                .type(dto.getType())
                .data(dto.getData())
                .generatedAt(dto.getGeneratedAt())
                .build();
    }
}
