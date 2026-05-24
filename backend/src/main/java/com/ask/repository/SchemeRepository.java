package com.ask.repository;

import com.ask.entity.Scheme;
import com.ask.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    
    @Query("SELECT s FROM Scheme s WHERE s.status = :status AND s.startDate <= :date AND s.endDate >= :date")
    List<Scheme> findActiveSchemesAtDate(@Param("status") EntityStatus status, @Param("date") LocalDate date);
}
