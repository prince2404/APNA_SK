package com.ask.repository;

import com.ask.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    Optional<Patient> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Page<Patient> findByStateId(Long stateId, Pageable pageable);
    Page<Patient> findByDistrictId(Long districtId, Pageable pageable);
    Page<Patient> findByBlockId(Long blockId, Pageable pageable);
    Page<Patient> findByStoreId(Long storeId, Pageable pageable);
}
