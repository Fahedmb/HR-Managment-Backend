package com.react.project.Mapper;

import com.react.project.Model.PerformanceEvaluation;
import com.react.project.DTO.PerformanceEvaluationDTO;
import com.react.project.Model.User;

public class PerformanceEvaluationMapper {

    public static PerformanceEvaluationDTO toDTO(PerformanceEvaluation evaluation) {
        return PerformanceEvaluationDTO.builder()
                .id(evaluation.getId())
                .userId(evaluation.getUser().getId())
                .evaluatorId(evaluation.getEvaluator().getId())
                .evaluationDate(evaluation.getEvaluationDate())
                .score(evaluation.getScore())
                .comments(evaluation.getComments())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }

    public static PerformanceEvaluation toEntity(PerformanceEvaluationDTO dto, User user, User evaluator) {
        return PerformanceEvaluation.builder()
                .id(dto.getId())
                .user(user)
                .evaluator(evaluator)
                .evaluationDate(dto.getEvaluationDate())
                .score(dto.getScore())
                .comments(dto.getComments())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
