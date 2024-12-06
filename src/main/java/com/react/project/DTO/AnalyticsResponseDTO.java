package com.react.project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponseDTO {
    private int workedDays;
    private int restDays;
    private int leaveDays;
}
