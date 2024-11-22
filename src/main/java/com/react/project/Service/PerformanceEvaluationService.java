package com.react.project.Service;

import com.react.project.DTO.PerformanceEvaluationDTO;

import java.util.List;

public interface PerformanceEvaluationService {
    PerformanceEvaluationDTO findById(Long id);
    List<PerformanceEvaluationDTO> findByUserId(Long userId);
    PerformanceEvaluationDTO create(PerformanceEvaluationDTO dto);
    PerformanceEvaluationDTO update(Long id, PerformanceEvaluationDTO dto);
    void delete(Long id);
}
