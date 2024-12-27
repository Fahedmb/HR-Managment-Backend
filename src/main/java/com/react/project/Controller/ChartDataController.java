package com.react.project.Controller;

import com.react.project.DTO.ChartDataDTO;
import com.react.project.Enumirator.LeaveStatus;
import com.react.project.Model.LeaveRequest;
import com.react.project.Model.User;
import com.react.project.Repository.LeaveRequestRepository;
import com.react.project.Repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChartDataController {

    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public ChartDataController(UserRepository userRepository,
                               LeaveRequestRepository leaveRequestRepository) {
        this.userRepository = userRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @GetMapping(value = "/chart-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChartDataDTO getChartData() {
        int currentYear = LocalDate.now().getYear();

        // 1) total employees * 40
        List<User> allUsers = userRepository.findAll();
        long employeesCount = allUsers
                .stream()
                .filter(u -> u.getRole().name().equals("EMPLOYEE"))
                .count();

        // For each of the 12 months => employeesCount * 40
        List<Integer> monthlyWorkingHours = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthlyWorkingHours.add((int) (employeesCount * 40));
        }

        // 2) monthly leave hours
        List<Integer> monthlyLeaveHours = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthlyLeaveHours.add(0);
        }

        List<LeaveRequest> leaves = leaveRequestRepository.findAll();
        for (LeaveRequest lr : leaves) {
            if (lr.getStatus() == LeaveStatus.APPROVED) {
                LocalDate start = lr.getStartDate();
                LocalDate end   = lr.getEndDate();

                // clamp to current year
                LocalDate yStart = LocalDate.of(currentYear, 1, 1);
                LocalDate yEnd   = LocalDate.of(currentYear, 12, 31);

                LocalDate s = start.isBefore(yStart) ? yStart : start;
                LocalDate e = end.isAfter(yEnd) ? yEnd : end;

                LocalDate d = s;
                while (!d.isAfter(e)) {
                    if (d.getYear() == currentYear) {
                        int monthIndex = d.getMonthValue() - 1;
                        int oldVal = monthlyLeaveHours.get(monthIndex);
                        monthlyLeaveHours.set(monthIndex, oldVal + 8);
                    }
                    d = d.plusDays(1);
                }
            }
        }

        return new ChartDataDTO(monthlyWorkingHours, monthlyLeaveHours);
    }
}
