package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.dashboard.DashboardResponse;
import com.ask.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping(ApiPaths.DASHBOARD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching dashboard data for user: {}", userDetails.getUsername());
        DashboardResponse response = dashboardService.getDashboardData(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.DASHBOARD));
    }
}
