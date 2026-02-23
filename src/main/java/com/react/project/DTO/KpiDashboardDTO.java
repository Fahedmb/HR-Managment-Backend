package com.react.project.DTO;

import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive KPI dashboard returned to HR / managers / employees based on who requests it.
 * Field names are aligned with the frontend TypeScript interfaces (HrKpiDashboard + EmployeeKpiDashboard).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDashboardDTO {

    // ---- Workforce overview (HR-level) ----
    private Long totalEmployees;
    private Long totalHrUsers;        // was totalHR
    private Long totalManagers;
    private Long newHiresThisMonth;
    private Long departmentCount;
    private Map<String, Long> departmentBreakdown;    // was employeesByDepartment

    // ---- Attendance & Hours ----
    private Double avgHoursWorkedThisMonth;
    private Double totalHoursWorkedThisMonth;
    private Long absentToday;
    private Long presentToday;
    private Double attendanceRatePercent;

    // ---- Leave (HR view) ----
    private Long pendingLeaveRequests;
    private Long totalLeaveRequestsThisYear;
    private Long approvedLeaveRequests;           // was approvedLeaveThisMonth
    private Long rejectedLeaveRequests;           // was rejectedLeaveThisMonth
    private Long pendingCancellationRequests;
    private Map<String, Long> leaveByType;
    private Double avgLeaveDaysPerEmployee;       // was avgLeaveDaysUsed

    // ---- Timesheet Schedules (HR view) ----
    private Long totalSchedulesSubmitted;
    private Long approvedSchedules;
    private Long pendingSchedules;

    // ---- Projects & Tasks ----
    private Long totalProjects;
    private Long activeProjects;
    private Long completedProjects;
    private Long overdueProjects;
    private Map<String, Long> projectsByStatus;
    private Map<String, Long> projectsByDepartment;
    private Long totalTasks;
    private Long completedTasks;                  // was tasksCompleted
    private Long overdueTasks;                    // was tasksOverdue
    private Long tasksInProgress;
    private Double taskCompletionRate;            // was taskCompletionRatePercent
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> tasksByStatus;

    // ---- Teams ----
    private Long totalTeams;

    // ---- Meetings ----
    private Long totalMeetings;                   // meetings this month (HR)
    private Long upcomingMeetings;
    private Long completedMeetings;

    // ---- Performance ----
    private Double avgPerformanceScore;
    private Long evaluationsThisYear;

    // ---- Employee personal KPI (filled only for /kpi/employee/{id}) ----
    private String employeeName;
    private String department;
    private String position;
    private Long myTotalTasks;
    private Long myCompletedTasks;
    private Long myInProgressTasks;
    private Long myOverdueTasks;
    private Long myLeaveBalance;
    private Long myUsedLeaveDays;
    private Long myPendingLeaveRequests;
    private List<String> myTeams;
    private List<String> myProjects;
    private Long myMeetingsThisMonth;
    private Double myPerformanceScore;
}
