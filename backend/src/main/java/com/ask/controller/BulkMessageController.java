package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.message.BulkMessageRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.message.BulkMessageLogResponse;
import com.ask.service.BulkMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.MESSAGES)
public class BulkMessageController {

    private final BulkMessageService bulkMessageService;

    @PostMapping("/send-bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<BulkMessageLogResponse>> sendBulkMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BulkMessageRequest request) {
        log.info("Sender {} initiating bulk message dispatch over {}", userDetails.getUsername(), request.getChannel());
        BulkMessageLogResponse response = bulkMessageService.sendBulkMessage(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Bulk message dispatch completed", ApiPaths.MESSAGES + "/send-bulk"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BulkMessageLogResponse>>> getBulkMessageHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Sender {} retrieving bulk message history log", userDetails.getUsername());
        PageResponse<BulkMessageLogResponse> response = bulkMessageService.getBulkMessageHistory(page, size, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.MESSAGES + "/history"));
    }
}
