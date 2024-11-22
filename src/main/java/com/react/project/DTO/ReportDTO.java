package com.react.project.DTO;

import com.react.project.Enumirator.ReportType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long id;
    private Long generatedById;
    private ReportType type;
    private byte[] data;
    private LocalDateTime generatedAt;
}
