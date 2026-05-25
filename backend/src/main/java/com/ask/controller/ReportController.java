package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.response.common.ApiResponse;
import com.ask.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.REPORTS)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // --- 1. Sales Report ---
    @GetMapping("/sales")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSalesReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> response = reportService.getSalesReport(storeId, startDate, endDate, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/sales"));
    }

    @GetMapping("/sales/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportSalesReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> data = reportService.getSalesReport(storeId, startDate, endDate, userDetails.getUsername());
        List<String> headers = Arrays.asList("Date", "Store Name", "Store Code", "Total Bills", "Total MRP (₹)", "Total Ask Price (₹)", "Total Discount (₹)", "Total GST (₹)", "Net Revenue (₹)");
        List<String> keys = Arrays.asList("date", "storeName", "storeCode", "totalBills", "totalMrp", "totalAskPrice", "totalDiscount", "totalGst", "netAmount");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "sales-report.csv");
    }

    // --- 2. Stock Report ---
    @GetMapping("/stock")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long categoryId) {
        List<Map<String, Object>> response = reportService.getStockReport(storeId, categoryId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/stock"));
    }

    @GetMapping("/stock/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportStockReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long categoryId) {
        List<Map<String, Object>> data = reportService.getStockReport(storeId, categoryId, userDetails.getUsername());
        List<String> headers = Arrays.asList("Store Name", "Product Name", "Brand", "Category", "Batch Number", "Expiry Date", "Available Quantity");
        List<String> keys = Arrays.asList("storeName", "productName", "brand", "category", "batchNumber", "expiryDate", "quantity");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "stock-report.csv");
    }

    // --- 3. Commission Report ---
    @GetMapping("/commission")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCommissionReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Long roleId) {
        List<Map<String, Object>> response = reportService.getCommissionReport(month, roleId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/commission"));
    }

    @GetMapping("/commission/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportCommissionReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Long roleId) {
        List<Map<String, Object>> data = reportService.getCommissionReport(month, roleId, userDetails.getUsername());
        List<String> headers = Arrays.asList("Month", "User Name", "Role", "Earned Amount (₹)", "Status");
        List<String> keys = Arrays.asList("month", "userName", "role", "amount", "status");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "commission-report.csv");
    }

    // --- 4. Patient Report ---
    @GetMapping("/patient")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPatientReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> response = reportService.getPatientReport(blockId, storeId, startDate, endDate, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/patient"));
    }

    @GetMapping("/patient/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportPatientReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> data = reportService.getPatientReport(blockId, storeId, startDate, endDate, userDetails.getUsername());
        List<String> headers = Arrays.asList("Patient ID", "Full Name", "Age", "Gender", "Phone", "Email", "Address", "Registered Date", "Store Name", "Block");
        List<String> keys = Arrays.asList("patientId", "fullName", "age", "gender", "phone", "email", "address", "registeredDate", "storeName", "block");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "patient-report.csv");
    }

    // --- 5. Bill Report ---
    @GetMapping("/bill")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBillReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> response = reportService.getBillReport(storeId, status, startDate, endDate, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/bill"));
    }

    @GetMapping("/bill/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportBillReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> data = reportService.getBillReport(storeId, status, startDate, endDate, userDetails.getUsername());
        List<String> headers = Arrays.asList("Bill Number", "Date", "Patient Name", "Store Name", "Net Amount (₹)", "Payment Mode", "Status");
        List<String> keys = Arrays.asList("billNumber", "date", "patientName", "storeName", "netAmount", "paymentMode", "status");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "bill-report.csv");
    }

    // --- 6. Expiry Report ---
    @GetMapping("/expiry")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExpiryReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> response = reportService.getExpiryReport(days, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/expiry"));
    }

    @GetMapping("/expiry/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportExpiryReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> data = reportService.getExpiryReport(days, userDetails.getUsername());
        List<String> headers = Arrays.asList("Store Name", "Product Name", "Batch Number", "Expiry Date", "Current Stock");
        List<String> keys = Arrays.asList("storeName", "productName", "batchNumber", "expiryDate", "quantity");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "expiry-report.csv");
    }

    // --- 7. User Activity Report ---
    @GetMapping("/activity")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW_ACTIVITY_LOG')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserActivityReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> response = reportService.getUserActivityReport(userId, action, startDate, endDate, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/activity"));
    }

    @GetMapping("/activity/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW_ACTIVITY_LOG')")
    public ResponseEntity<byte[]> exportUserActivityReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> data = reportService.getUserActivityReport(userId, action, startDate, endDate, userDetails.getUsername());
        List<String> headers = Arrays.asList("Timestamp", "User Name", "Role", "Action", "Entity Type", "Entity ID", "Description");
        List<String> keys = Arrays.asList("timestamp", "userName", "role", "action", "entityType", "entityId", "description");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "activity-report.csv");
    }

    // --- 8. Revenue Report ---
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRevenueReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> response = reportService.getRevenueReport(stateId, districtId, startDate, endDate, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/revenue"));
    }

    @GetMapping("/revenue/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportRevenueReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> data = reportService.getRevenueReport(stateId, districtId, startDate, endDate, userDetails.getUsername());
        List<String> headers = Arrays.asList("State", "District", "Total Revenue (₹)", "Total Invoices");
        List<String> keys = Arrays.asList("state", "district", "totalRevenue", "totalInvoices");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "revenue-report.csv");
    }

    // --- 9. Low Stock Report ---
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLowStockReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId) {
        List<Map<String, Object>> response = reportService.getLowStockReport(storeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.REPORTS + "/low-stock"));
    }

    @GetMapping("/low-stock/export")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_REPORTS_DOWNLOAD_REPORTS')")
    public ResponseEntity<byte[]> exportLowStockReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId) {
        List<Map<String, Object>> data = reportService.getLowStockReport(storeId, userDetails.getUsername());
        List<String> headers = Arrays.asList("Store Name", "Product Name", "Current Stock", "Min Threshold");
        List<String> keys = Arrays.asList("storeName", "productName", "currentStock", "minThreshold");
        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        return createCsvResponse(csvBytes, "low-stock-report.csv");
    }

    private ResponseEntity<byte[]> createCsvResponse(byte[] bytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
