package com.react.project.ServiceImpl;

import com.react.project.DTO.KpiDashboardDTO;
import com.react.project.Enumirator.*;
import com.react.project.Model.*;
import com.react.project.Repository.*;
import com.react.project.Service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiServiceImpl implements KpiService {

    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final PerformanceEvaluationRepository performanceEvaluationRepository;

    @Override
    public KpiDashboardDTO getHrDashboard() {
        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = now.atTime(23, 59, 59);

        List<User> allUsers = userRepository.findAll();
        long totalEmployees = allUsers.stream().filter(u -> u.getRole() == Role.EMPLOYEE).count();
        long totalHR = allUsers.stream().filter(u -> u.getRole() == Role.HR).count();
        long newHires = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfMonth))
                .count();

        Set<String> departments = allUsers.stream()
                .filter(u -> u.getDepartment() != null && !u.getDepartment().isBlank())
                .map(User::getDepartment).collect(Collectors.toSet());
        Map<String, Long> byDept = allUsers.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(User::getDepartment, Collectors.counting()));

        // Leave KPIs
        List<LeaveRequest> allLeaves = leaveRequestRepository.findAll();
        long pendingLeaves = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long approvedThisMonth = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED
                        && l.getUpdatedAt() != null && l.getUpdatedAt().isAfter(startOfMonth)).count();
        long rejectedThisMonth = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.REJECTED
                        && l.getUpdatedAt() != null && l.getUpdatedAt().isAfter(startOfMonth)).count();
        long pendingCancellation = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING_CANCELLATION).count();
        Map<String, Long> leaveByType = allLeaves.stream()
                .collect(Collectors.groupingBy(l -> l.getType().name(), Collectors.counting()));
        OptionalDouble avgLeaveDays = allUsers.stream()
                .mapToInt(User::getUsedDaysThisYear).average();

        // Attendance KPIs
        List<TimeSheet> allSheets = timeSheetRepository.findAll();
        List<TimeSheet> thisMonthSheets = allSheets.stream()
                .filter(ts -> !ts.getDate().isBefore(firstOfMonth) && !ts.getDate().isAfter(now))
                .collect(Collectors.toList());
        double totalHoursThisMonth = thisMonthSheets.stream()
                .mapToDouble(ts -> ts.getHoursWorked() != null ? ts.getHoursWorked() : 0).sum();
        double avgHours = allUsers.isEmpty() ? 0 : totalHoursThisMonth / allUsers.size();

        Set<Long> workingTodayIds = timeSheetRepository.findAll().stream()
                .filter(ts -> ts.getDate().equals(now))
                .map(ts -> ts.getUser().getId())
                .collect(Collectors.toSet());
        long presentToday = workingTodayIds.size();
        long absentToday = totalEmployees - presentToday;
        double attendanceRate = totalEmployees == 0 ? 0 : (presentToday * 100.0) / totalEmployees;

        // Project & Task KPIs
        List<Project> projects = projectRepository.findAll();
        long totalProjects = projects.size();
        long activeProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
        long completedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();

        List<Task> allTasks = taskRepository.findAll();
        long totalTasks = allTasks.size();
        long tasksDone = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long tasksOverdue = taskRepository.findOverdueTasks(now).size();
        long tasksInProgress = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        double taskCompletionRate = totalTasks == 0 ? 0 : (tasksDone * 100.0) / totalTasks;
        Map<String, Long> tasksByPriority = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        Map<String, Long> tasksByStatus = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        // Teams
        long totalTeams = teamRepository.count();

        // Meetings
        long meetingsThisMonth = meetingRepository.findByStartTimeBetween(startOfMonth, endOfMonth).size();
        long upcomingMeetings = meetingRepository.findByStartTimeBetween(
                LocalDateTime.now(), LocalDateTime.now().plusDays(30)).size();

        // Performance
        OptionalDouble avgPerf = performanceEvaluationRepository.findAll().stream()
                .filter(p -> p.getScore() != null)
                .mapToDouble(p -> p.getScore().doubleValue()).average();

        return KpiDashboardDTO.builder()
                .totalEmployees(totalEmployees)
                .totalHR(totalHR)
                .newHiresThisMonth(newHires)
                .departmentCount((long) departments.size())
                .employeesByDepartment(byDept)
                .avgHoursWorkedThisMonth(Math.round(avgHours * 100.0) / 100.0)
                .totalHoursWorkedThisMonth(totalHoursThisMonth)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .attendanceRatePercent(Math.round(attendanceRate * 100.0) / 100.0)
                .pendingLeaveRequests(pendingLeaves)
                .approvedLeaveThisMonth(approvedThisMonth)
                .rejectedLeaveThisMonth(rejectedThisMonth)
                .pendingCancellationRequests(pendingCancellation)
                .leaveByType(leaveByType)
                .avgLeaveDaysUsed(avgLeaveDays.isPresent() ? Math.round(avgLeaveDays.getAsDouble() * 100.0) / 100.0 : 0)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalTasks(totalTasks)
                .tasksCompleted(tasksDone)
                .tasksOverdue(tasksOverdue)
                .tasksInProgress(tasksInProgress)
                .taskCompletionRatePercent(Math.round(taskCompletionRate * 100.0) / 100.0)
                .tasksByPriority(tasksByPriority)
                .tasksByStatus(tasksByStatus)
                .totalTeams(totalTeams)
                .meetingsThisMonth(meetingsThisMonth)
                .upcomingMeetings(upcomingMeetings)
                .avgPerformanceScore(avgPerf.isPresent() ? Math.round(avgPerf.getAsDouble() * 100.0) / 100.0 : null)
                .build();
    }

    @Override
    public KpiDashboardDTO getProjectDashboard(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        long total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long overdue = tasks.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(LocalDate.now())
                        && t.getStatus() != TaskStatus.DONE).count();
        double rate = total == 0 ? 0 : (done * 100.0) / total;

        Map<String, Long> byPriority = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        Map<String, Long> byStatus = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        List<Team> teams = teamRepository.findByProjectId(projectId);

        return KpiDashboardDTO.builder()
                .totalTasks(total)
                .tasksCompleted(done)
                .tasksInProgress(inProgress)
                .tasksOverdue(overdue)
                .taskCompletionRatePercent(Math.round(rate * 100.0) / 100.0)
                .tasksByPriority(byPriority)
                .tasksByStatus(byStatus)
                .totalTeams((long) teams.size())
                .build();
    }

    @Override
    public KpiDashboardDTO getEmployeeDashboard(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);

        // Leave
        List<LeaveRequest> userLeaves = leaveRequestRepository.findByUserId(userId);
        int usedDays = userRepository.findById(userId).map(User::getUsedDaysThisYear).orElse(0);
        int balance = 30 - usedDays;

        // Hours this month
        List<TimeSheet> sheets = timeSheetRepository.findByUserId(userId).stream()
                .filter(ts -> !ts.getDate().isBefore(firstOfMonth) && !ts.getDate().isAfter(now))
                .collect(Collectors.toList());
        double hoursThisMonth = sheets.stream()
                .mapToDouble(ts -> ts.getHoursWorked() != null ? ts.getHoursWorked() : 0).sum();

        // Tasks
        List<Task> myTasks = taskRepository.findByAssignedToId(userId);
        long myPending = myTasks.stream().filter(t -> t.getStatus() != TaskStatus.DONE).count();
        long myDone = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        // Upcoming meetings
        long upcoming = meetingRepository.findAllForUser(userId).stream()
                .filter(m -> m.getStartTime().isAfter(LocalDateTime.now()))
                .count();

        return KpiDashboardDTO.builder()
                .userLeaveBalance(balance)
                .userUsedLeaveDays(usedDays)
                .userPendingTasks(myPending)
                .userCompletedTasks(myDone)
                .userHoursThisMonth(hoursThisMonth)
                .userUpcomingMeetings(upcoming)
                .pendingLeaveRequests((long) userLeaves.stream()
                        .filter(l -> l.getStatus() == LeaveStatus.PENDING).count())
                .build();
    }
}
