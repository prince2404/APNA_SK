package com.ask.service;

import com.ask.dto.request.commission.CommissionConfigRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.commission.CommissionConfigResponse;
import com.ask.dto.response.commission.CommissionSummaryResponse;
import com.ask.entity.CommissionEntry;

import java.util.List;

public interface CommissionService {
    List<CommissionConfigResponse> getConfigs(String currentUserEmail);
    CommissionConfigResponse updateConfig(CommissionConfigRequest request, String currentUserEmail);
    PageResponse<CommissionEntry> getCommissions(Long userId, Long roleId, String month, String status, int page, int size, String currentUserEmail);
    List<CommissionSummaryResponse> getCommissionSummary(String month, String currentUserEmail);
}
