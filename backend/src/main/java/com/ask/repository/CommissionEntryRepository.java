package com.ask.repository;

import com.ask.entity.CommissionEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommissionEntryRepository extends JpaRepository<CommissionEntry, Long>, JpaSpecificationExecutor<CommissionEntry> {
    Page<CommissionEntry> findByUserId(Long userId, Pageable pageable);
    List<CommissionEntry> findByBillId(Long billId);

    @Query("SELECT e.user.id, e.user.fullName, e.role.name, e.month, SUM(e.amount) " +
           "FROM CommissionEntry e " +
           "WHERE e.month = :month AND e.status != 'CANCELLED' " +
           "GROUP BY e.user.id, e.user.fullName, e.role.name, e.month")
    List<Object[]> findMonthlySummary(@Param("month") String month);
}
