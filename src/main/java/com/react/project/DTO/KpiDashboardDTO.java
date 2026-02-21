package com.react.project.DTO;

import lombok.*;

import java.util.Map;

/**
 * Comprehensive KPI dashboard returned to HR / managers / employees based on who requests it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDashboardDTO {

    // ---- Workforce overview (HR-level) ----
    private Long totalEmployees;
    private Long totalHR;
    private Long newHiresThisMonth;
    private Long departmentCount;
    private Map<String, Long> employeesByDepartment;

    // ---- Attendance & Hours ----
    private Double avgHoursWorkedThisMonth;      // org-wide or per-user
    private Double totalHoursWorkedThisMonth;
    private Long absentToday;                     // no timesheet entry today
    private Long presentToday;
    private Double attendanceRatePercent;         // present / total

    // ---- Leave ----
    private Long pendingLeaveRequests;
    private Long approvedLeaveThisMonth;
    private Long rejectedLeaveThisMonth;
    private Long pendingCancellationRequests;
    private Map<String, Long> leaveByType;      // VACATION, SICK, etc.
    private Double avgLeaveDaysUsed;

    // ---- Projects & Tasks ----
    private Long totalProjects;
    private Long activeProjects;
    private Long completedProjects;
    private Long totalTasks;
    private Long tasksCompleted;
    private Long tasksOverdue;
    private Long tasksInProgress;
    private Double taskCompletionRatePercent;
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> tasksByStatus;

    // ---- Teams ----
    private Long totalTeams;

    // ---- Meetings ----
    private Long meetingsThisMonth;
    private Long upcomingMeetings;

    // ---- Performance ----
    private Double avgPerformanceScore;

    // ---- User-level (when fetched for a single employee) ----
    private Integer userLeaveBalance;
    private Integer userUsedLeaveDays;
    private Long userPendingTasks;
    private Long userCompletedTasks;
    private Double userHoursThisMonth;
    private Long userUpcomingMeetings;
}
