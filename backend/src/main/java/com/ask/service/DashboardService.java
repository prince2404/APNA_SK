package com.ask.service;

import com.ask.dto.response.dashboard.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardData(String currentUserEmail);
}
