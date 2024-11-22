package com.react.project.Service;

import com.react.project.DTO.ReportDTO;

import java.util.List;

public interface ReportService {
    List<ReportDTO> findByGeneratedById(Long generatedById);
    ReportDTO create(ReportDTO reportDTO);
    void delete(Long id);
}
