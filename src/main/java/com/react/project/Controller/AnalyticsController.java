package com.react.project.Controller;

import com.react.project.DTO.AnalyticsResponseDTO;
import com.react.project.Service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{userId}")
    public AnalyticsResponseDTO getAnalytics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "Monthly") String period
    ) {
        return analyticsService.getAnalytics(userId, period);
    }
}
