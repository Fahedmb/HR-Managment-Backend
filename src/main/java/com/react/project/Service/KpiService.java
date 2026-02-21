package com.react.project.Service;

import com.react.project.DTO.KpiDashboardDTO;

public interface KpiService {
    /** Full HR-level dashboard with all org-wide KPIs */
    KpiDashboardDTO getHrDashboard();

    /** Project/team-leader view: stats for a specific project */
    KpiDashboardDTO getProjectDashboard(Long projectId);

    /** Employee personal KPI view */
    KpiDashboardDTO getEmployeeDashboard(Long userId);
}
