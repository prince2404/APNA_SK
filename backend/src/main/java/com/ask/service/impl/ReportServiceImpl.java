package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.entity.*;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.*;
import com.ask.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final EntityManager entityManager;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void enforceGeographicScope(User currentUser, Map<String, Object> queryParams) {
        String role = currentUser.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            return;
        }

        if (RoleConstants.STATE_ADMIN.equals(role)) {
            queryParams.put("stateId", currentUser.getState().getId());
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            queryParams.put("districtId", currentUser.getDistrict().getId());
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            queryParams.put("blockId", currentUser.getBlock().getId());
        } else {
            // Receptionist, Volunteer, Pharmacist
            if (currentUser.getStore() != null) {
                queryParams.put("storeId", currentUser.getStore().getId());
            }
        }
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now().minusDays(30).atStartOfDay();
        }
        return LocalDate.parse(dateStr.trim()).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now().atTime(LocalTime.MAX);
        }
        return LocalDate.parse(dateStr.trim()).atTime(LocalTime.MAX);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSalesReport(Long storeId, String startDate, String endDate, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        StringBuilder jpql = new StringBuilder("SELECT b.billDate, b.store.name, b.store.code, COUNT(b), " +
                "SUM(b.totalMrp), SUM(b.totalAskPrice), SUM(b.totalDiscount), SUM(b.totalGst), SUM(b.netAmount) " +
                "FROM Bill b WHERE b.billDate BETWEEN :start AND :end AND b.status = 'ACTIVE' ");

        if (storeId != null) {
            jpql.append("AND b.store.id = :storeId ");
        } else if (scope.containsKey("storeId")) {
            jpql.append("AND b.store.id = :storeId ");
        } else if (scope.containsKey("blockId")) {
            jpql.append("AND b.store.block.id = :blockId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND b.store.block.district.id = :districtId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND b.store.block.district.state.id = :stateId ");
        }

        jpql.append("GROUP BY b.billDate, b.store.name, b.store.code ORDER BY b.billDate DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("start", start);
        query.setParameter("end", end);

        if (storeId != null) {
            query.setParameter("storeId", storeId);
        } else if (scope.containsKey("storeId")) {
            query.setParameter("storeId", scope.get("storeId"));
        } else if (scope.containsKey("blockId")) {
            query.setParameter("blockId", scope.get("blockId"));
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", ((LocalDateTime) r[0]).toLocalDate().toString());
            map.put("storeName", r[1]);
            map.put("storeCode", r[2]);
            map.put("totalBills", r[3]);
            map.put("totalMrp", r[4]);
            map.put("totalAskPrice", r[5]);
            map.put("totalDiscount", r[6]);
            map.put("totalGst", r[7]);
            map.put("netAmount", r[8]);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStockReport(Long storeId, Long categoryId, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        StringBuilder jpql = new StringBuilder("SELECT s.store.name, s.product.name, s.product.brand, " +
                "s.product.category.name, s.batchNumber, s.expiryDate, s.quantity " +
                "FROM StockStore s WHERE 1=1 ");

        if (storeId != null) {
            jpql.append("AND s.store.id = :storeId ");
        } else if (scope.containsKey("storeId")) {
            jpql.append("AND s.store.id = :storeId ");
        } else if (scope.containsKey("blockId")) {
            jpql.append("AND s.store.block.id = :blockId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND s.store.block.district.id = :districtId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND s.store.block.district.state.id = :stateId ");
        }

        if (categoryId != null) {
            jpql.append("AND s.product.category.id = :categoryId ");
        }

        jpql.append("ORDER BY s.store.name ASC, s.product.name ASC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);

        if (storeId != null) {
            query.setParameter("storeId", storeId);
        } else if (scope.containsKey("storeId")) {
            query.setParameter("storeId", scope.get("storeId"));
        } else if (scope.containsKey("blockId")) {
            query.setParameter("blockId", scope.get("blockId"));
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("storeName", r[0]);
            map.put("productName", r[1]);
            map.put("brand", r[2]);
            map.put("category", r[3]);
            map.put("batchNumber", r[4]);
            map.put("expiryDate", r[5].toString());
            map.put("quantity", r[6]);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCommissionReport(String month, Long roleId, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        String currentRole = currentUser.getRole().getName();

        StringBuilder jpql = new StringBuilder("SELECT c.month, c.user.fullName, c.role.name, SUM(c.amount), c.status " +
                "FROM CommissionEntry c WHERE 1=1 ");

        // Scope check
        if (RoleConstants.STATE_ADMIN.equals(currentRole)) {
            jpql.append("AND c.user.state.id = :stateId ");
        } else if (RoleConstants.DISTRICT_ADMIN.equals(currentRole)) {
            jpql.append("AND c.user.district.id = :districtId ");
        } else if (RoleConstants.BLOCK_ADMIN.equals(currentRole)) {
            jpql.append("AND c.user.block.id = :blockId ");
        } else if (!RoleConstants.SUPER_ADMIN.equals(currentRole) && !RoleConstants.SYSTEM_ADMIN.equals(currentRole)) {
            jpql.append("AND c.user.id = :userId ");
        }

        if (month != null && !month.trim().isEmpty()) {
            jpql.append("AND c.month = :month ");
        }
        if (roleId != null) {
            jpql.append("AND c.role.id = :roleId ");
        }

        jpql.append("GROUP BY c.month, c.user.fullName, c.role.name, c.status ORDER BY c.month DESC, SUM(c.amount) DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);

        if (RoleConstants.STATE_ADMIN.equals(currentRole)) {
            query.setParameter("stateId", currentUser.getState().getId());
        } else if (RoleConstants.DISTRICT_ADMIN.equals(currentRole)) {
            query.setParameter("districtId", currentUser.getDistrict().getId());
        } else if (RoleConstants.BLOCK_ADMIN.equals(currentRole)) {
            query.setParameter("blockId", currentUser.getBlock().getId());
        } else if (!RoleConstants.SUPER_ADMIN.equals(currentRole) && !RoleConstants.SYSTEM_ADMIN.equals(currentRole)) {
            query.setParameter("userId", currentUser.getId());
        }

        if (month != null && !month.trim().isEmpty()) {
            query.setParameter("month", month.trim());
        }
        if (roleId != null) {
            query.setParameter("roleId", roleId);
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("month", r[0]);
            map.put("userName", r[1]);
            map.put("role", r[2]);
            map.put("amount", r[3]);
            map.put("status", r[4].toString());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPatientReport(Long blockId, Long storeId, String startDate, String endDate, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        StringBuilder jpql = new StringBuilder("SELECT p.id, p.fullName, p.age, p.gender, p.phone, p.email, " +
                "p.address, p.createdAt, p.store.name, p.block.name " +
                "FROM Patient p WHERE p.createdAt BETWEEN :start AND :end ");

        if (storeId != null) {
            jpql.append("AND p.store.id = :storeId ");
        } else if (blockId != null) {
            jpql.append("AND p.block.id = :blockId ");
        } else if (scope.containsKey("storeId")) {
            jpql.append("AND p.store.id = :storeId ");
        } else if (scope.containsKey("blockId")) {
            jpql.append("AND p.block.id = :blockId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND p.district.id = :districtId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND p.state.id = :stateId ");
        }

        jpql.append("ORDER BY p.createdAt DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("start", start);
        query.setParameter("end", end);

        if (storeId != null) {
            query.setParameter("storeId", storeId);
        } else if (blockId != null) {
            query.setParameter("blockId", blockId);
        } else if (scope.containsKey("storeId")) {
            query.setParameter("storeId", scope.get("storeId"));
        } else if (scope.containsKey("blockId")) {
            query.setParameter("blockId", scope.get("blockId"));
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("patientId", r[0]);
            map.put("fullName", r[1]);
            map.put("age", r[2]);
            map.put("gender", r[3].toString());
            map.put("phone", r[4]);
            map.put("email", r[5]);
            map.put("address", r[6]);
            map.put("registeredDate", ((LocalDateTime) r[7]).toLocalDate().toString());
            map.put("storeName", r[8] != null ? r[8] : "N/A");
            map.put("block", r[9]);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBillReport(Long storeId, String status, String startDate, String endDate, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        StringBuilder jpql = new StringBuilder("SELECT b.billNumber, b.billDate, b.patient.fullName, " +
                "b.store.name, b.netAmount, b.paymentMode, b.status " +
                "FROM Bill b WHERE b.billDate BETWEEN :start AND :end ");

        if (storeId != null) {
            jpql.append("AND b.store.id = :storeId ");
        } else if (scope.containsKey("storeId")) {
            jpql.append("AND b.store.id = :storeId ");
        } else if (scope.containsKey("blockId")) {
            jpql.append("AND b.store.block.id = :blockId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND b.store.block.district.id = :districtId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND b.store.block.district.state.id = :stateId ");
        }

        if (status != null && !status.trim().isEmpty()) {
            jpql.append("AND b.status = :status ");
        }

        jpql.append("ORDER BY b.billDate DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("start", start);
        query.setParameter("end", end);

        if (storeId != null) {
            query.setParameter("storeId", storeId);
        } else if (scope.containsKey("storeId")) {
            query.setParameter("storeId", scope.get("storeId"));
        } else if (scope.containsKey("blockId")) {
            query.setParameter("blockId", scope.get("blockId"));
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        if (status != null && !status.trim().isEmpty()) {
            query.setParameter("status", com.ask.enums.BillStatus.valueOf(status.toUpperCase().trim()));
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("billNumber", r[0]);
            map.put("date", ((LocalDateTime) r[1]).toString());
            map.put("patientName", r[2]);
            map.put("storeName", r[3]);
            map.put("netAmount", r[4]);
            map.put("paymentMode", r[5].toString());
            map.put("status", r[6].toString());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getExpiryReport(Integer days, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        LocalDate limitDate = LocalDate.now().plusDays(days != null ? days : 30);

        StringBuilder jpql = new StringBuilder("SELECT s.store.name, s.product.name, s.batchNumber, s.expiryDate, s.quantity " +
                "FROM StockStore s WHERE s.expiryDate <= :limitDate ");

        if (scope.containsKey("storeId")) {
            jpql.append("AND s.store.id = :storeId ");
        } else if (scope.containsKey("blockId")) {
            jpql.append("AND s.store.block.id = :blockId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND s.store.block.district.id = :districtId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND s.store.block.district.state.id = :stateId ");
        }

        jpql.append("ORDER BY s.expiryDate ASC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("limitDate", limitDate);

        if (scope.containsKey("storeId")) {
            query.setParameter("storeId", scope.get("storeId"));
        } else if (scope.containsKey("blockId")) {
            query.setParameter("blockId", scope.get("blockId"));
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("storeName", r[0]);
            map.put("productName", r[1]);
            map.put("batchNumber", r[2]);
            map.put("expiryDate", r[3].toString());
            map.put("quantity", r[4]);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserActivityReport(Long userId, String action, String startDate, String endDate, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        
        // Super Admin / System Admin only
        String role = currentUser.getRole().getName();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            throw new AccessDeniedException("Access denied. Admin role required to view audit logs.");
        }

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        StringBuilder jpql = new StringBuilder("SELECT a.createdAt, a.user.fullName, a.user.role.name, " +
                "a.action, a.entityType, a.entityId, a.description " +
                "FROM AuditLog a WHERE a.createdAt BETWEEN :start AND :end ");

        if (userId != null) {
            jpql.append("AND a.user.id = :userId ");
        }
        if (action != null && !action.trim().isEmpty()) {
            jpql.append("AND a.action = :action ");
        }

        jpql.append("ORDER BY a.createdAt DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("start", start);
        query.setParameter("end", end);

        if (userId != null) {
            query.setParameter("userId", userId);
        }
        if (action != null && !action.trim().isEmpty()) {
            query.setParameter("action", action.trim());
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("timestamp", r[0].toString());
            map.put("userName", r[1]);
            map.put("role", r[2]);
            map.put("action", r[3]);
            map.put("entityType", r[4]);
            map.put("entityId", r[5]);
            map.put("description", r[6]);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueReport(Long stateId, Long districtId, String startDate, String endDate, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        StringBuilder jpql = new StringBuilder("SELECT b.store.block.district.state.name, b.store.block.district.name, " +
                "SUM(b.netAmount), COUNT(b) " +
                "FROM Bill b WHERE b.billDate BETWEEN :start AND :end AND b.status = 'ACTIVE' ");

        if (stateId != null) {
            jpql.append("AND b.store.block.district.state.id = :stateId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND b.store.block.district.state.id = :stateId ");
        }

        if (districtId != null) {
            jpql.append("AND b.store.block.district.id = :districtId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND b.store.block.district.id = :districtId ");
        }

        jpql.append("GROUP BY b.store.block.district.state.name, b.store.block.district.name ORDER BY SUM(b.netAmount) DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("start", start);
        query.setParameter("end", end);

        if (stateId != null) {
            query.setParameter("stateId", stateId);
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        if (districtId != null) {
            query.setParameter("districtId", districtId);
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("state", r[0]);
            map.put("district", r[1]);
            map.put("totalRevenue", r[2]);
            map.put("totalInvoices", r[3]);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLowStockReport(Long storeId, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Map<String, Object> scope = new HashMap<>();
        enforceGeographicScope(currentUser, scope);

        StringBuilder jpql = new StringBuilder("SELECT s.store.name, s.product.name, s.quantity, s.product.minStockThreshold " +
                "FROM StockStore s WHERE s.quantity < s.product.minStockThreshold ");

        if (storeId != null) {
            jpql.append("AND s.store.id = :storeId ");
        } else if (scope.containsKey("storeId")) {
            jpql.append("AND s.store.id = :storeId ");
        } else if (scope.containsKey("blockId")) {
            jpql.append("AND s.store.block.id = :blockId ");
        } else if (scope.containsKey("districtId")) {
            jpql.append("AND s.store.block.district.id = :districtId ");
        } else if (scope.containsKey("stateId")) {
            jpql.append("AND s.store.block.district.state.id = :stateId ");
        }

        jpql.append("ORDER BY s.store.name ASC, s.quantity ASC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);

        if (storeId != null) {
            query.setParameter("storeId", storeId);
        } else if (scope.containsKey("storeId")) {
            query.setParameter("storeId", scope.get("storeId"));
        } else if (scope.containsKey("blockId")) {
            query.setParameter("blockId", scope.get("blockId"));
        } else if (scope.containsKey("districtId")) {
            query.setParameter("districtId", scope.get("districtId"));
        } else if (scope.containsKey("stateId")) {
            query.setParameter("stateId", scope.get("stateId"));
        }

        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("storeName", r[0]);
            map.put("productName", r[1]);
            map.put("currentStock", r[2]);
            map.put("minThreshold", r[3]);
            result.add(map);
        }
        return result;
    }

    @Override
    public byte[] exportToCsv(List<Map<String, Object>> data, List<String> headers, List<String> keys) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");

        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (String key : keys) {
                Object val = row.get(key);
                String valStr = val == null ? "" : val.toString();
                if (valStr.contains(",") || valStr.contains("\"") || valStr.contains("\n")) {
                    valStr = "\"" + valStr.replace("\"", "\"\"") + "\"";
                }
                values.add(valStr);
            }
            sb.append(String.join(",", values)).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
