package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.response.dashboard.DashboardResponse;
import com.ask.entity.*;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.UserRepository;
import com.ask.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        String role = currentUser.getRole().getName();

        List<DashboardResponse.MetricCard> metrics = new ArrayList<>();
        List<DashboardResponse.ChartDataPoint> trendData = new ArrayList<>();
        List<DashboardResponse.ChartDataPoint> breakdownData = new ArrayList<>();
        List<DashboardResponse.TopProductPoint> topProducts = new ArrayList<>();
        List<DashboardResponse.RecentActivityPoint> recentActivity = new ArrayList<>();

        // 1. Gather Metrics and Scoped breakdowns
        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            // Platform Wide
            BigDecimal totalSales = getSalesSum(null, "global");
            Long storeCount = getCount("SELECT COUNT(s) FROM Store s WHERE s.status = 'ACTIVE'");
            Long patientCount = getCount("SELECT COUNT(p) FROM Patient p WHERE p.status = 'ACTIVE'");
            Long lowStockCount = getCount("SELECT COUNT(s) FROM StockStore s WHERE s.quantity < s.product.minStockThreshold");

            metrics.add(new DashboardResponse.MetricCard("Total Sales", "₹" + formatAmount(totalSales), "Cumulative", "CURRENCY"));
            metrics.add(new DashboardResponse.MetricCard("Active Stores", storeCount.toString(), "All States", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("Registered Patients", patientCount.toString(), "System-wide", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("Low Stock Warnings", lowStockCount.toString(), "Needs attention", "ALERT"));

            // Trend
            trendData = getMonthlyTrendSales(null, "global");

            // State Breakdown
            breakdownData = getGeographicBreakdown("Bihar, Uttar Pradesh, Jharkhand", "state", null);

        } else if (RoleConstants.STATE_ADMIN.equals(role)) {
            // State scoped
            Long stateId = currentUser.getState().getId();
            BigDecimal totalSales = getSalesSum(stateId, "state");
            Long storeCount = getCount("SELECT COUNT(s) FROM Store s WHERE s.block.district.state.id = " + stateId + " AND s.status = 'ACTIVE'");
            Long patientCount = getCount("SELECT COUNT(p) FROM Patient p WHERE p.state.id = " + stateId + " AND p.status = 'ACTIVE'");
            Long lowStockCount = getCount("SELECT COUNT(s) FROM StockStore s WHERE s.store.block.district.state.id = " + stateId + " AND s.quantity < s.product.minStockThreshold");

            metrics.add(new DashboardResponse.MetricCard("State Sales", "₹" + formatAmount(totalSales), currentUser.getState().getName(), "CURRENCY"));
            metrics.add(new DashboardResponse.MetricCard("State Stores", storeCount.toString(), "Active Centres", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("State Patients", patientCount.toString(), "Registered Patients", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("State Low Stock Alerts", lowStockCount.toString(), "Restock recommended", "ALERT"));

            trendData = getMonthlyTrendSales(stateId, "state");
            breakdownData = getGeographicBreakdown("", "district", stateId);

        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            // District scoped
            Long districtId = currentUser.getDistrict().getId();
            BigDecimal totalSales = getSalesSum(districtId, "district");
            Long storeCount = getCount("SELECT COUNT(s) FROM Store s WHERE s.block.district.id = " + districtId + " AND s.status = 'ACTIVE'");
            Long patientCount = getCount("SELECT COUNT(p) FROM Patient p WHERE p.district.id = " + districtId + " AND p.status = 'ACTIVE'");
            Long lowStockCount = getCount("SELECT COUNT(s) FROM StockStore s WHERE s.store.block.district.id = " + districtId + " AND s.quantity < s.product.minStockThreshold");

            metrics.add(new DashboardResponse.MetricCard("District Sales", "₹" + formatAmount(totalSales), currentUser.getDistrict().getName(), "CURRENCY"));
            metrics.add(new DashboardResponse.MetricCard("District Stores", storeCount.toString(), "Active", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("District Patients", patientCount.toString(), "Registered Patients", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("District Low Stock Alerts", lowStockCount.toString(), "Immediate restock needed", "ALERT"));

            trendData = getMonthlyTrendSales(districtId, "district");
            breakdownData = getGeographicBreakdown("", "block", districtId);

        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            // Block scoped
            Long blockId = currentUser.getBlock().getId();
            BigDecimal totalSales = getSalesSum(blockId, "block");
            Long storeCount = getCount("SELECT COUNT(s) FROM Store s WHERE s.block.id = " + blockId + " AND s.status = 'ACTIVE'");
            Long patientCount = getCount("SELECT COUNT(p) FROM Patient p WHERE p.block.id = " + blockId + " AND p.status = 'ACTIVE'");
            Long lowStockCount = getCount("SELECT COUNT(s) FROM StockStore s WHERE s.store.block.id = " + blockId + " AND s.quantity < s.product.minStockThreshold");

            metrics.add(new DashboardResponse.MetricCard("Block Sales", "₹" + formatAmount(totalSales), currentUser.getBlock().getName(), "CURRENCY"));
            metrics.add(new DashboardResponse.MetricCard("Block Stores", storeCount.toString(), "Active Centres", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("Block Patients", patientCount.toString(), "Registered Patients", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("Block Low Stock Alerts", lowStockCount.toString(), "Action required", "ALERT"));

            trendData = getMonthlyTrendSales(blockId, "block");
            breakdownData = getGeographicBreakdown("", "store", blockId);

        } else if (RoleConstants.PHARMACIST.equals(role)) {
            // Pharmacist stats (central warehouse focus)
            Long productCatalogCount = getCount("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'");
            Long categoryCount = getCount("SELECT COUNT(c) FROM ProductCategory c WHERE c.status = 'ACTIVE'");
            Long pendingTransfers = getCount("SELECT COUNT(t) FROM TransferOrder t WHERE t.status = 'PENDING'");
            Long centralLowStock = getCount("SELECT COUNT(s) FROM StockCentral s WHERE s.quantity < 100"); // arbitrary warning

            metrics.add(new DashboardResponse.MetricCard("Product Catalog Size", productCatalogCount.toString(), "Active products", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("Product Categories", categoryCount.toString(), "Mapped categories", "NUMBER"));
            metrics.add(new DashboardResponse.MetricCard("Pending Transfer Orders", pendingTransfers.toString(), "Warehouse dispatch", "ALERT"));
            metrics.add(new DashboardResponse.MetricCard("Central Stock Warnings", centralLowStock.toString(), "Below safety levels", "ALERT"));

            trendData = getMonthlyTrendSales(null, "global");
            // Category breakdown of central stock
            breakdownData = getCentralStockCategoryBreakdown();

        } else {
            // Receptionist / Volunteer (Store focus)
            Long storeId = currentUser.getStore() != null ? currentUser.getStore().getId() : null;
            if (storeId != null) {
                BigDecimal todaySalesSum = getStoreTodaySalesSum(storeId);
                Long todayInvoices = getCount("SELECT COUNT(b) FROM Bill b WHERE b.store.id = " + storeId + " AND b.createdAt >= :todayStart");
                BigDecimal mtdSalesSum = getStoreMtdSalesSum(storeId);
                Long lowStockAlerts = getCount("SELECT COUNT(s) FROM StockStore s WHERE s.store.id = " + storeId + " AND s.quantity < s.product.minStockThreshold");

                metrics.add(new DashboardResponse.MetricCard("Today's Sales", "₹" + formatAmount(todaySalesSum), todayInvoices + " bills generated", "CURRENCY"));
                metrics.add(new DashboardResponse.MetricCard("Month-to-Date Sales", "₹" + formatAmount(mtdSalesSum), "Current month", "CURRENCY"));
                metrics.add(new DashboardResponse.MetricCard("Low Stock Items", lowStockAlerts.toString(), "This store location", "ALERT"));

                trendData = getMonthlyTrendSales(storeId, "store");
                breakdownData = getStorePaymentModeBreakdown(storeId);
            }
        }

        // 2. Top Selling Products (Global or Scoped)
        topProducts = getTopSellingProducts(currentUser);

        // 3. Recent activity list
        recentActivity = getRecentActivities(currentUser);

        return DashboardResponse.builder()
                .metrics(metrics)
                .trendData(trendData)
                .breakdownData(breakdownData)
                .topProducts(topProducts)
                .recentActivity(recentActivity)
                .build();
    }

    private BigDecimal getSalesSum(Long scopeId, String type) {
        StringBuilder jpql = new StringBuilder("SELECT SUM(b.netAmount) FROM Bill b WHERE b.status = 'ACTIVE' ");
        if ("state".equals(type)) {
            jpql.append("AND b.store.block.district.state.id = :scopeId ");
        } else if ("district".equals(type)) {
            jpql.append("AND b.store.block.district.id = :scopeId ");
        } else if ("block".equals(type)) {
            jpql.append("AND b.store.block.id = :scopeId ");
        }

        TypedQuery<BigDecimal> query = entityManager.createQuery(jpql.toString(), BigDecimal.class);
        if (scopeId != null) {
            query.setParameter("scopeId", scopeId);
        }
        BigDecimal sum = query.getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal getStoreTodaySalesSum(Long storeId) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
                "SELECT SUM(b.netAmount) FROM Bill b WHERE b.store.id = :storeId AND b.status = 'ACTIVE' AND b.createdAt >= :todayStart",
                BigDecimal.class);
        query.setParameter("storeId", storeId);
        query.setParameter("todayStart", LocalDate.now().atStartOfDay());
        BigDecimal sum = query.getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal getStoreMtdSalesSum(Long storeId) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
                "SELECT SUM(b.netAmount) FROM Bill b WHERE b.store.id = :storeId AND b.status = 'ACTIVE' AND b.createdAt >= :monthStart",
                BigDecimal.class);
        query.setParameter("storeId", storeId);
        query.setParameter("monthStart", LocalDate.now().withDayOfMonth(1).atStartOfDay());
        BigDecimal sum = query.getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private Long getCount(String jpql) {
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        if (jpql.contains(":todayStart")) {
            query.setParameter("todayStart", LocalDate.now().atStartOfDay());
        }
        return query.getSingleResult();
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    private List<DashboardResponse.ChartDataPoint> getMonthlyTrendSales(Long scopeId, String type) {
        // Fetch sales for last 6 months
        List<DashboardResponse.ChartDataPoint> list = new ArrayList<>();
        LocalDateTime sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay();

        StringBuilder jpql = new StringBuilder("SELECT FUNCTION('MONTH', b.billDate), SUM(b.netAmount) " +
                "FROM Bill b WHERE b.billDate >= :sixMonthsAgo AND b.status = 'ACTIVE' ");

        if ("store".equals(type)) {
            jpql.append("AND b.store.id = :scopeId ");
        } else if ("block".equals(type)) {
            jpql.append("AND b.store.block.id = :scopeId ");
        } else if ("district".equals(type)) {
            jpql.append("AND b.store.block.district.id = :scopeId ");
        } else if ("state".equals(type)) {
            jpql.append("AND b.store.block.district.state.id = :scopeId ");
        }

        jpql.append("GROUP BY FUNCTION('MONTH', b.billDate) ORDER BY FUNCTION('MONTH', b.billDate) ASC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setParameter("sixMonthsAgo", sixMonthsAgo);
        if (scopeId != null) {
            query.setParameter("scopeId", scopeId);
        }

        List<Object[]> rows = query.getResultList();
        for (Object[] r : rows) {
            int monthNum = ((Number) r[0]).intValue();
            String monthName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal val = (BigDecimal) r[1];
            list.add(new DashboardResponse.ChartDataPoint(monthName, val.doubleValue(), 0.0));
        }

        return list;
    }

    private List<DashboardResponse.ChartDataPoint> getGeographicBreakdown(String defaultNames, String levelType, Long parentId) {
        List<DashboardResponse.ChartDataPoint> list = new ArrayList<>();
        StringBuilder jpql = new StringBuilder();

        if ("state".equals(levelType)) {
            jpql.append("SELECT b.store.block.district.state.name, SUM(b.netAmount) " +
                    "FROM Bill b WHERE b.status = 'ACTIVE' GROUP BY b.store.block.district.state.name");
        } else if ("district".equals(levelType)) {
            jpql.append("SELECT b.store.block.district.name, SUM(b.netAmount) " +
                    "FROM Bill b WHERE b.store.block.district.state.id = :parentId AND b.status = 'ACTIVE' GROUP BY b.store.block.district.name");
        } else if ("block".equals(levelType)) {
            jpql.append("SELECT b.store.block.name, SUM(b.netAmount) " +
                    "FROM Bill b WHERE b.store.block.district.id = :parentId AND b.status = 'ACTIVE' GROUP BY b.store.block.name");
        } else if ("store".equals(levelType)) {
            jpql.append("SELECT b.store.name, SUM(b.netAmount) " +
                    "FROM Bill b WHERE b.store.block.id = :parentId AND b.status = 'ACTIVE' GROUP BY b.store.name");
        }

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        if (parentId != null) {
            query.setParameter("parentId", parentId);
        }

        List<Object[]> rows = query.getResultList();
        for (Object[] r : rows) {
            String name = (String) r[0];
            BigDecimal val = (BigDecimal) r[1];
            list.add(new DashboardResponse.ChartDataPoint(name, val.doubleValue(), 0.0));
        }
        return list;
    }

    private List<DashboardResponse.ChartDataPoint> getStorePaymentModeBreakdown(Long storeId) {
        List<DashboardResponse.ChartDataPoint> list = new ArrayList<>();
        TypedQuery<Object[]> query = entityManager.createQuery(
                "SELECT b.paymentMode, SUM(b.netAmount) FROM Bill b WHERE b.store.id = :storeId AND b.status = 'ACTIVE' GROUP BY b.paymentMode",
                Object[].class);
        query.setParameter("storeId", storeId);

        List<Object[]> rows = query.getResultList();
        for (Object[] r : rows) {
            String mode = r[0].toString();
            BigDecimal val = (BigDecimal) r[1];
            list.add(new DashboardResponse.ChartDataPoint(mode, val.doubleValue(), 0.0));
        }
        return list;
    }

    private List<DashboardResponse.ChartDataPoint> getCentralStockCategoryBreakdown() {
        List<DashboardResponse.ChartDataPoint> list = new ArrayList<>();
        TypedQuery<Object[]> query = entityManager.createQuery(
                "SELECT s.product.category.name, SUM(s.quantity) FROM StockCentral s GROUP BY s.product.category.name",
                Object[].class);

        List<Object[]> rows = query.getResultList();
        for (Object[] r : rows) {
            String cat = (String) r[0];
            Long qty = (Long) r[1];
            list.add(new DashboardResponse.ChartDataPoint(cat, qty.doubleValue(), 0.0));
        }
        return list;
    }

    private List<DashboardResponse.TopProductPoint> getTopSellingProducts(User user) {
        List<DashboardResponse.TopProductPoint> list = new ArrayList<>();
        String role = user.getRole().getName();

        StringBuilder jpql = new StringBuilder("SELECT item.product.name, SUM(item.quantity), SUM(item.subtotal) " +
                "FROM BillItem item WHERE item.bill.status = 'ACTIVE' ");

        if (RoleConstants.STATE_ADMIN.equals(role)) {
            jpql.append("AND item.bill.store.block.district.state.id = :scopeId ");
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            jpql.append("AND item.bill.store.block.district.id = :scopeId ");
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            jpql.append("AND item.bill.store.block.id = :scopeId ");
        } else if (user.getStore() != null && !RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            jpql.append("AND item.bill.store.id = :scopeId ");
        }

        jpql.append("GROUP BY item.product.name ORDER BY SUM(item.quantity) DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setMaxResults(5);

        if (RoleConstants.STATE_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getState().getId());
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getDistrict().getId());
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getBlock().getId());
        } else if (user.getStore() != null && !RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getStore().getId());
        }

        List<Object[]> rows = query.getResultList();
        for (Object[] r : rows) {
            String name = (String) r[0];
            Long qty = (Long) r[1];
            BigDecimal rev = (BigDecimal) r[2];
            list.add(new DashboardResponse.TopProductPoint(name, qty.intValue(), rev.doubleValue()));
        }
        return list;
    }

    private List<DashboardResponse.RecentActivityPoint> getRecentActivities(User user) {
        List<DashboardResponse.RecentActivityPoint> list = new ArrayList<>();
        String role = user.getRole().getName();

        StringBuilder jpql = new StringBuilder("SELECT b.billNumber, b.netAmount, b.createdAt, b.store.name " +
                "FROM Bill b WHERE 1=1 ");

        if (RoleConstants.STATE_ADMIN.equals(role)) {
            jpql.append("AND b.store.block.district.state.id = :scopeId ");
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            jpql.append("AND b.store.block.district.id = :scopeId ");
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            jpql.append("AND b.store.block.id = :scopeId ");
        } else if (user.getStore() != null && !RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            jpql.append("AND b.store.id = :scopeId ");
        }

        jpql.append("ORDER BY b.createdAt DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        query.setMaxResults(5);

        if (RoleConstants.STATE_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getState().getId());
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getDistrict().getId());
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getBlock().getId());
        } else if (user.getStore() != null && !RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            query.setParameter("scopeId", user.getStore().getId());
        }

        List<Object[]> rows = query.getResultList();
        for (Object[] r : rows) {
            String num = (String) r[0];
            BigDecimal val = (BigDecimal) r[1];
            LocalDateTime date = (LocalDateTime) r[2];
            String storeName = (String) r[3];
            list.add(new DashboardResponse.RecentActivityPoint(
                    String.format("Sales invoice %s generated at %s for ₹%.2f", num, storeName, val.doubleValue()),
                    date.toString(),
                    "SALE"
            ));
        }
        return list;
    }
}
