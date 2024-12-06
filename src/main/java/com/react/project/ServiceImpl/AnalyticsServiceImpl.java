package com.react.project.ServiceImpl;

import com.react.project.DTO.AnalyticsResponseDTO;
import com.react.project.Enumirator.DayOfWeekEnum;
import com.react.project.Exception.UserException;
import com.react.project.Model.LeaveRequest;
import com.react.project.Model.TimeSheet;
import com.react.project.Model.TimesheetSchedule;
import com.react.project.Model.User;
import com.react.project.Repository.LeaveRequestRepository;
import com.react.project.Repository.TimeSheetRepository;
import com.react.project.Repository.TimesheetScheduleRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimesheetScheduleRepository timesheetScheduleRepository;
    private static final int TOTAL_HOURS_PER_WEEK = 40;

    public AnalyticsServiceImpl(UserRepository userRepository, TimeSheetRepository timeSheetRepository, LeaveRequestRepository leaveRequestRepository, TimesheetScheduleRepository timesheetScheduleRepository) {
        this.userRepository = userRepository;
        this.timeSheetRepository = timeSheetRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.timesheetScheduleRepository = timesheetScheduleRepository;
    }

    @Override
    public AnalyticsResponseDTO getAnalytics(Long userId, String period) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = period.equalsIgnoreCase("Yearly") ? 0 : now.getMonthValue();

        List<TimesheetSchedule> schedules = timesheetScheduleRepository.findByUserId(userId);
        Optional<TimesheetSchedule> optSchedule = schedules.stream().max(Comparator.comparing(TimesheetSchedule::getCreatedAt));

        int workedDays = 0;
        int leaveDays = 0;
        int totalScheduled = 0;

        if (!optSchedule.isPresent()) {
            workedDays = calculateWorkedDays(userId, year, month);
            leaveDays = calculateLeaveDays(userId, year, month);
            totalScheduled = 0;
        } else {
            TimesheetSchedule schedule = optSchedule.get();
            workedDays = calculateWorkedDays(userId, year, month);
            leaveDays = calculateLeaveDays(userId, year, month);
            totalScheduled = calculateTotalScheduledWorkDays(schedule, year, month);
        }

        int restDays = Math.max(totalScheduled - workedDays - leaveDays, 0);
        return new AnalyticsResponseDTO(workedDays, restDays, leaveDays);
    }

    private int calculateWorkedDays(Long userId, int year, int month) {
        List<TimeSheet> sheets = timeSheetRepository.findByUserId(userId);
        return (int) sheets.stream()
                .filter(ts -> isInPeriod(ts.getDate(), year, month))
                .filter(ts -> ts.getHoursWorked() != null && ts.getHoursWorked() > 0)
                .count();
    }

    private int calculateLeaveDays(Long userId, int year, int month) {
        List<LeaveRequest> leaves = leaveRequestRepository.findByUserId(userId);
        int count = 0;
        for (LeaveRequest lr : leaves) {
            if (lr.getStatus().name().equals("APPROVED")) {
                LocalDate start = lr.getStartDate();
                LocalDate end = lr.getEndDate();
                LocalDate date = start;
                while (!date.isAfter(end)) {
                    if (isInPeriod(date, year, month)) {
                        count++;
                    }
                    date = date.plusDays(1);
                }
            }
        }
        return count;
    }

    private int calculateTotalScheduledWorkDays(TimesheetSchedule schedule, int year, int month) {
        if (month == 0) {
            int total = 0;
            for (int m = 1; m <= 12; m++) {
                total += countChosenDaysInMonth(schedule, year, m);
            }
            return total;
        } else {
            return countChosenDaysInMonth(schedule, year, month);
        }
    }

    private int countChosenDaysInMonth(TimesheetSchedule schedule, int year, int month) {
        if (schedule.getChosenDays() == null || schedule.getChosenDays().isEmpty()) return 0;
        YearMonth ym = YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();
        int count = 0;
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = LocalDate.of(year, month, d);
            if (!date.isBefore(schedule.getCreatedAt().toLocalDate()) && isScheduledDay(schedule, date)) {
                count++;
            }
        }
        return count;
    }

    private boolean isScheduledDay(TimesheetSchedule schedule, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return schedule.getChosenDays().contains(convertDayOfWeek(dayOfWeek));
    }

    private boolean isInPeriod(LocalDate date, int year, int month) {
        if (month == 0) {
            return date.getYear() == year;
        } else {
            return date.getYear() == year && date.getMonthValue() == month;
        }
    }

    private DayOfWeekEnum convertDayOfWeek(DayOfWeek dw) {
        return DayOfWeekEnum.valueOf(dw.name());
    }
}
