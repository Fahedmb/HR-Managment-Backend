package com.react.project.DTO;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceEvaluationDTO {
    private Long id;
    private Long userId;
    private Long evaluatorId;
    private LocalDate evaluationDate;
    private Integer score;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
