package com.react.project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceDTO {
    private int maxDaysPerYear;
    private int usedDaysThisYear;
    private int remainingDays;
}
