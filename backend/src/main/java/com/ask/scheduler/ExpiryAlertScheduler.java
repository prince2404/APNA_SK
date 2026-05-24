package com.ask.scheduler;

import com.ask.constants.RoleConstants;
import com.ask.entity.StockCentral;
import com.ask.entity.StockStore;
import com.ask.entity.User;
import com.ask.enums.NotificationType;
import com.ask.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryAlertScheduler {

    private final EntityManager entityManager;
    private final NotificationService notificationService;

    // Runs once a day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void runExpiryScan() {
        log.info("Starting daily batch expiry scanning job...");

        LocalDate limitDate = LocalDate.now().plusDays(30);
        LocalDate today = LocalDate.now();

        // 1. Scan Store Stock
        TypedQuery<StockStore> storeQuery = entityManager.createQuery(
                "SELECT s FROM StockStore s WHERE s.expiryDate BETWEEN :today AND :limitDate AND s.quantity > 0",
                StockStore.class);
        storeQuery.setParameter("today", today);
        storeQuery.setParameter("limitDate", limitDate);
        List<StockStore> expiringStoreStock = storeQuery.getResultList();

        log.info("Found {} expiring batches in store stocks", expiringStoreStock.size());
        for (StockStore ss : expiringStoreStock) {
            Long storeId = ss.getStore().getId();
            
            // Find active staff for this store
            TypedQuery<User> staffQuery = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.store.id = :storeId",
                    User.class);
            staffQuery.setParameter("storeId", storeId);
            List<User> staffList = staffQuery.getResultList();

            String message = String.format("Stock batch %s for %s is expiring on %s (Qty: %d). Please dispose or return.",
                    ss.getBatchNumber(), ss.getProduct().getName(), ss.getExpiryDate(), ss.getQuantity());

            for (User staff : staffList) {
                notificationService.sendNotification(
                        staff,
                        NotificationType.EXPIRY_ALERT,
                        "Store Stock Expiry Warning",
                        message,
                        "STOCK_STORE",
                        ss.getId()
                );
            }
        }

        // 2. Scan Central Stock
        TypedQuery<StockCentral> centralQuery = entityManager.createQuery(
                "SELECT s FROM StockCentral s WHERE s.expiryDate BETWEEN :today AND :limitDate AND s.quantity > 0 AND s.status = 'AVAILABLE'",
                StockCentral.class);
            centralQuery.setParameter("today", today);
            centralQuery.setParameter("limitDate", limitDate);
        List<StockCentral> expiringCentralStock = centralQuery.getResultList();

        log.info("Found {} expiring batches in central warehouse", expiringCentralStock.size());
        if (!expiringCentralStock.isEmpty()) {
            // Find all active pharmacists
            TypedQuery<User> pharmQuery = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.role.name = :roleName",
                    User.class);
            pharmQuery.setParameter("roleName", RoleConstants.PHARMACIST);
            List<User> pharmacists = pharmQuery.getResultList();

            for (StockCentral sc : expiringCentralStock) {
                String message = String.format("Central stock batch %s for %s is expiring on %s (Qty: %d).",
                        sc.getBatchNumber(), sc.getProduct().getName(), sc.getExpiryDate(), sc.getQuantity());

                for (User pharm : pharmacists) {
                    notificationService.sendNotification(
                            pharm,
                            NotificationType.EXPIRY_ALERT,
                            "Central Warehouse Expiry Alert",
                            message,
                            "STOCK_CENTRAL",
                            sc.getId()
                    );
                }
            }
        }

        log.info("Batch expiry scanning job completed.");
    }
}
