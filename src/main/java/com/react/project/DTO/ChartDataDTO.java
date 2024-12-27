package com.react.project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {
    private List<Integer> monthlyWorkingHours;
    private List<Integer> monthlyLeaveHours;
}
