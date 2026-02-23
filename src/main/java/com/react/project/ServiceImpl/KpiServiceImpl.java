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
    private final TimesheetScheduleRepository timesheetScheduleRepository;
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
        LocalDateTime startOfYear = now.withDayOfYear(1).atStartOfDay();

        List<User> allUsers = userRepository.findAll();
        long totalEmployees = allUsers.stream().filter(u -> u.getRole() == Role.EMPLOYEE).count();
        long totalHR        = allUsers.stream().filter(u -> u.getRole() == Role.HR).count();
        long newHires       = allUsers.stream()
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
        long pendingLeaves    = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long approvedLeaves   = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
        long rejectedLeaves   = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();
        long pendingCancellation = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING_CANCELLATION).count();
        Map<String, Long> leaveByType = allLeaves.stream()
                .collect(Collectors.groupingBy(l -> l.getType().name(), Collectors.counting()));
        OptionalDouble avgLeaveDays = allUsers.stream().mapToInt(User::getUsedDaysThisYear).average();

        // Attendance KPIs
        List<TimeSheet> allSheets = timeSheetRepository.findAll();
        List<TimeSheet> thisMonthSheets = allSheets.stream()
                .filter(ts -> !ts.getDate().isBefore(firstOfMonth) && !ts.getDate().isAfter(now))
                .collect(Collectors.toList());
        double totalHoursThisMonth = thisMonthSheets.stream()
                .mapToDouble(ts -> ts.getHoursWorked() != null ? ts.getHoursWorked() : 0).sum();
        double avgHours = allUsers.isEmpty() ? 0 : totalHoursThisMonth / allUsers.size();

        Set<Long> workingTodayIds = allSheets.stream()
                .filter(ts -> ts.getDate().equals(now))
                .map(ts -> ts.getUser().getId()).collect(Collectors.toSet());
        long presentToday = workingTodayIds.size();
        long absentToday = totalEmployees - presentToday;
        double attendanceRate = totalEmployees == 0 ? 0 : (presentToday * 100.0) / totalEmployees;

        // Timesheet schedules
        List<TimesheetSchedule> allSchedules = timesheetScheduleRepository.findAll();
        long totalSchedules  = allSchedules.size();
        long approvedScheds  = allSchedules.stream().filter(s -> s.getStatus() == TimesheetStatus.APPROVED).count();
        long pendingScheds   = allSchedules.stream().filter(s -> s.getStatus() == TimesheetStatus.PENDING).count();

        // Project & Task KPIs
        List<Project> projects = projectRepository.findAll();
        long totalProjects     = projects.size();
        long activeProjects    = projects.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
        long completedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        long overdueProjects   = projects.stream()
                .filter(p -> p.getDeadline() != null && p.getDeadline().isBefore(now)
                          && p.getStatus() != ProjectStatus.COMPLETED && p.getStatus() != ProjectStatus.CANCELLED)
                .count();
        Map<String, Long> projectsByStatus = projects.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));
        Map<String, Long> projectsByDepartment = projects.stream()
                .filter(p -> p.getDepartment() != null)
                .collect(Collectors.groupingBy(Project::getDepartment, Collectors.counting()));

        List<Task> allTasks    = taskRepository.findAll();
        long totalTasks        = allTasks.size();
        long tasksDone         = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long tasksOverdue      = taskRepository.findOverdueTasks(now).size();
        long tasksInProgress   = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        double taskCompletionRate = totalTasks == 0 ? 0 : (tasksDone * 100.0) / totalTasks;
        Map<String, Long> tasksByPriority = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        Map<String, Long> tasksByStatus = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        // Teams
        long totalTeams = teamRepository.count();

        // Meetings
        long meetingsThisMonth = meetingRepository.findByStartTimeBetween(startOfMonth, endOfMonth).size();
        long upcomingMeetings  = meetingRepository.findByStartTimeBetween(
                LocalDateTime.now(), LocalDateTime.now().plusDays(30)).size();
        long completedMeetings = meetingRepository.findAll().stream()
                .filter(m -> m.getStatus() == MeetingStatus.COMPLETED).count();

        // Performance
        List<PerformanceEvaluation> allEvals = performanceEvaluationRepository.findAll();
        OptionalDouble avgPerf = allEvals.stream()
                .filter(p -> p.getScore() != null)
                .mapToDouble(p -> p.getScore().doubleValue()).average();
        long evaluationsThisYear = allEvals.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfYear))
                .count();

        return KpiDashboardDTO.builder()
                .totalEmployees(totalEmployees)
                .totalHrUsers(totalHR)
                .newHiresThisMonth(newHires)
                .departmentCount((long) departments.size())
                .departmentBreakdown(byDept)
                .avgHoursWorkedThisMonth(Math.round(avgHours * 100.0) / 100.0)
                .totalHoursWorkedThisMonth(totalHoursThisMonth)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .attendanceRatePercent(Math.round(attendanceRate * 100.0) / 100.0)
                .pendingLeaveRequests(pendingLeaves)
                .totalLeaveRequestsThisYear((long) allLeaves.size())
                .approvedLeaveRequests(approvedLeaves)
                .rejectedLeaveRequests(rejectedLeaves)
                .pendingCancellationRequests(pendingCancellation)
                .leaveByType(leaveByType)
                .avgLeaveDaysPerEmployee(avgLeaveDays.isPresent()
                        ? Math.round(avgLeaveDays.getAsDouble() * 100.0) / 100.0 : 0)
                .totalSchedulesSubmitted(totalSchedules)
                .approvedSchedules(approvedScheds)
                .pendingSchedules(pendingScheds)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .overdueProjects(overdueProjects)
                .projectsByStatus(projectsByStatus)
                .projectsByDepartment(projectsByDepartment)
                .totalTasks(totalTasks)
                .completedTasks(tasksDone)
                .overdueTasks(tasksOverdue)
                .tasksInProgress(tasksInProgress)
                .taskCompletionRate(Math.round(taskCompletionRate * 100.0) / 100.0)
                .tasksByPriority(tasksByPriority)
                .tasksByStatus(tasksByStatus)
                .totalTeams(totalTeams)
                .totalMeetings(meetingsThisMonth)
                .upcomingMeetings(upcomingMeetings)
                .completedMeetings(completedMeetings)
                .avgPerformanceScore(avgPerf.isPresent()
                        ? Math.round(avgPerf.getAsDouble() * 100.0) / 100.0 : null)
                .evaluationsThisYear(evaluationsThisYear)
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
        Map<String, Long> byStatus   = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        List<Team> teams = teamRepository.findByProjectId(projectId);

        return KpiDashboardDTO.builder()
                .totalTasks(total)
                .completedTasks(done)
                .tasksInProgress(inProgress)
                .overdueTasks(overdue)
                .taskCompletionRate(Math.round(rate * 100.0) / 100.0)
                .tasksByPriority(byPriority)
                .tasksByStatus(byStatus)
                .totalTeams((long) teams.size())
                .build();
    }

    @Override
    public KpiDashboardDTO getEmployeeDashboard(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstOfMonth.atStartOfDay();
        LocalDateTime endOfMonth   = now.atTime(23, 59, 59);

        // User info
        User user = userRepository.findById(userId).orElse(null);
        String employeeName = user != null
                ? (user.getFirstName() + " " + user.getLastName()).trim() : "";
        String department = user != null && user.getDepartment() != null ? user.getDepartment() : "";
        String position   = user != null && user.getPosition() != null   ? user.getPosition()   : "";
        int usedDays = user != null ? user.getUsedDaysThisYear() : 0;
        int balance  = 30 - usedDays;

        // Leave
        List<LeaveRequest> userLeaves = leaveRequestRepository.findByUserId(userId);
        long pendingLeaves = userLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();

        // Tasks assigned to this employee
        List<Task> myTasks    = taskRepository.findByAssignedToId(userId);
        long totalTasks       = myTasks.size();
        long doneTasks        = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long inProgressTasks  = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long overdueTaskCount = myTasks.stream()
                .filter(t -> t.getDeadline() != null
                          && t.getDeadline().isBefore(now)
                          && t.getStatus() != TaskStatus.DONE)
                .count();

        // Teams the employee belongs to
        List<Team> myTeamsList = teamRepository.findAll().stream()
                .filter(t -> t.getMembers() != null && t.getMembers().stream()
                        .anyMatch(m -> m.getUser() != null && m.getUser().getId().equals(userId)))
                .collect(Collectors.toList());
        List<String> myTeamNames = myTeamsList.stream()
                .map(Team::getName).collect(Collectors.toList());

        // Projects from those teams
        List<String> myProjectNames = myTeamsList.stream()
                .map(t -> t.getProject() != null ? t.getProject().getName() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Meetings this month that include the employee
        long meetingsThisMonth = meetingRepository.findAllForUser(userId).stream()
                .filter(m -> m.getStartTime() != null
                          && !m.getStartTime().isBefore(startOfMonth)
                          && !m.getStartTime().isAfter(endOfMonth))
                .count();

        // Performance score (latest average)
        OptionalDouble perfScore = performanceEvaluationRepository.findAll().stream()
                .filter(p -> p.getUser() != null
                          && p.getUser().getId().equals(userId)
                          && p.getScore() != null)
                .mapToDouble(p -> p.getScore().doubleValue())
                .average();

        return KpiDashboardDTO.builder()
                .employeeName(employeeName)
                .department(department)
                .position(position)
                .myTotalTasks(totalTasks)
                .myCompletedTasks(doneTasks)
                .myInProgressTasks(inProgressTasks)
                .myOverdueTasks(overdueTaskCount)
                .myLeaveBalance((long) balance)
                .myUsedLeaveDays((long) usedDays)
                .myPendingLeaveRequests(pendingLeaves)
                .myTeams(myTeamNames)
                .myProjects(myProjectNames)
                .myMeetingsThisMonth(meetingsThisMonth)
                .myPerformanceScore(perfScore.isPresent()
                        ? Math.round(perfScore.getAsDouble() * 10.0) / 10.0 : null)
                .build();
    }
}
