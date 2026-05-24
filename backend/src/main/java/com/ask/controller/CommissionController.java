package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.commission.CommissionConfigRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.commission.CommissionConfigResponse;
import com.ask.dto.response.commission.CommissionSummaryResponse;
import com.ask.entity.CommissionEntry;
import com.ask.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping(ApiPaths.COMMISSION_CONFIG)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<CommissionConfigResponse>>> getConfigs(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CommissionConfigResponse> response = commissionService.getConfigs(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.COMMISSION_CONFIG));
    }

    @PutMapping(ApiPaths.COMMISSION_CONFIG)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<CommissionConfigResponse>> updateConfig(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CommissionConfigRequest request) {
        CommissionConfigResponse response = commissionService.updateConfig(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Commission config updated successfully", ApiPaths.COMMISSION_CONFIG));
    }

    @GetMapping(ApiPaths.COMMISSIONS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<CommissionEntry>>> getCommissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CommissionEntry> response = commissionService.getCommissions(userId, roleId, month, status, page, size, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.COMMISSIONS));
    }

    @GetMapping(ApiPaths.COMMISSIONS + "/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<CommissionSummaryResponse>>> getCommissionSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String month) {
        List<CommissionSummaryResponse> response = commissionService.getCommissionSummary(month, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.COMMISSIONS + "/summary"));
    }
}
