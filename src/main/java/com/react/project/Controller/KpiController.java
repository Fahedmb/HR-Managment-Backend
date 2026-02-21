package com.react.project.Controller;

import com.react.project.DTO.KpiDashboardDTO;
import com.react.project.Service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kpi")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    /** Full organisation-level dashboard – HR only */
    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR')")
    public KpiDashboardDTO hrDashboard() {
        return kpiService.getHrDashboard();
    }

    /** Project / team-leader view */
    @GetMapping("/project/{projectId}")
    public KpiDashboardDTO projectDashboard(@PathVariable Long projectId) {
        return kpiService.getProjectDashboard(projectId);
    }

    /** Employee personal KPI card */
    @GetMapping("/employee/{userId}")
    public KpiDashboardDTO employeeDashboard(@PathVariable Long userId) {
        return kpiService.getEmployeeDashboard(userId);
    }
}
