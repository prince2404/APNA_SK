package com.ask.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
    List<Map<String, Object>> getSalesReport(Long storeId, String startDate, String endDate, String currentUserEmail);
    List<Map<String, Object>> getStockReport(Long storeId, Long categoryId, String currentUserEmail);
    List<Map<String, Object>> getCommissionReport(String month, Long roleId, String currentUserEmail);
    List<Map<String, Object>> getPatientReport(Long blockId, Long storeId, String startDate, String endDate, String currentUserEmail);
    List<Map<String, Object>> getBillReport(Long storeId, String status, String startDate, String endDate, String currentUserEmail);
    List<Map<String, Object>> getExpiryReport(Integer days, String currentUserEmail);
    List<Map<String, Object>> getUserActivityReport(Long userId, String action, String startDate, String endDate, String currentUserEmail);
    List<Map<String, Object>> getRevenueReport(Long stateId, Long districtId, String startDate, String endDate, String currentUserEmail);
    List<Map<String, Object>> getLowStockReport(Long storeId, String currentUserEmail);

    byte[] exportToCsv(List<Map<String, Object>> data, List<String> headers, List<String> keys);
}
