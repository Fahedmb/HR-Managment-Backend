package com.react.project.Service;

import com.react.project.DTO.AnalyticsResponseDTO;

public interface AnalyticsService {
    AnalyticsResponseDTO getAnalytics(Long userId, String period);
}
