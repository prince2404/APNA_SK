package com.ask.repository;

import com.ask.entity.HealthCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface HealthCardRepository extends JpaRepository<HealthCard, Long>, JpaSpecificationExecutor<HealthCard> {
    Optional<HealthCard> findByCardNumber(String cardNumber);
    Optional<HealthCard> findByPatientId(Long patientId);
    Page<HealthCard> findByStoreId(Long storeId, Pageable pageable);
    boolean existsByCardNumber(String cardNumber);
}
