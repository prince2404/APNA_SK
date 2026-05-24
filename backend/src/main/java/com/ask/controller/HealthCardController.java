package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.healthcard.HealthCardMemberRequest;
import com.ask.dto.request.healthcard.HealthCardRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.healthcard.HealthCardResponse;
import com.ask.service.HealthCardService;
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
@RequestMapping(ApiPaths.HEALTH_CARDS)
@RequiredArgsConstructor
public class HealthCardController {

    private final HealthCardService healthCardService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<HealthCardResponse>> issueHealthCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody HealthCardRequest request) {
        HealthCardResponse response = healthCardService.issueHealthCard(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Health card issued successfully", ApiPaths.HEALTH_CARDS));
    }

    @GetMapping("/number/{cardNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<HealthCardResponse>> getHealthCardByNumber(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String cardNumber) {
        HealthCardResponse response = healthCardService.getHealthCardByNumber(cardNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.HEALTH_CARDS + "/number/" + cardNumber));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<HealthCardResponse>> getHealthCardByPatientId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long patientId) {
        HealthCardResponse response = healthCardService.getHealthCardByPatientId(patientId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.HEALTH_CARDS + "/patient/" + patientId));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<HealthCardResponse>> addFamilyMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody HealthCardMemberRequest request) {
        HealthCardResponse response = healthCardService.addFamilyMember(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Family member added successfully", ApiPaths.HEALTH_CARDS + "/" + id + "/members"));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<Void>> removeFamilyMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long memberId) {
        healthCardService.removeFamilyMember(id, memberId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Family member removed successfully", ApiPaths.HEALTH_CARDS + "/" + id + "/members/" + memberId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<PageResponse<HealthCardResponse>>> getHealthCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<HealthCardResponse> response = healthCardService.getHealthCards(page, size, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.HEALTH_CARDS));
    }
}
