package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.billing.BillRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.billing.BillResponse;
import com.ask.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping(ApiPaths.BILLS)
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_BILLING_CREATE_BILL')")
    public ResponseEntity<ApiResponse<BillResponse>> createBill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BillRequest request) {
        BillResponse response = billService.createBill(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Bill generated successfully", ApiPaths.BILLS));
    }

    @GetMapping("/number/{billNumber}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_BILLING_VIEW_BILLS')")
    public ResponseEntity<ApiResponse<BillResponse>> getBillByNumber(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String billNumber) {
        BillResponse response = billService.getBillByNumber(billNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.BILLS + "/number/" + billNumber));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_BILLING_VIEW_BILLS')")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        BillResponse response = billService.getBillById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.BILLS + "/" + id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_BILLING_VIEW_BILLS')")
    public ResponseEntity<ApiResponse<PageResponse<BillResponse>>> getBills(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<BillResponse> response = billService.getBills(storeId, page, size, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.BILLS));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_BILLING_CANCEL_BILL')")
    public ResponseEntity<ApiResponse<BillResponse>> cancelBill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam String cancelReason) {
        BillResponse response = billService.cancelBill(id, cancelReason, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Bill cancelled successfully", ApiPaths.BILLS + "/" + id + "/cancel"));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_BILLING_VIEW_BILLS')")
    public ResponseEntity<byte[]> generateBillPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        byte[] pdfBytes = billService.generateBillPdf(id, userDetails.getUsername());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
