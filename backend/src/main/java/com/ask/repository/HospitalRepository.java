package com.ask.repository;

import com.ask.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HospitalRepository extends JpaRepository<Hospital, Long>, JpaSpecificationExecutor<Hospital> {
    Page<Hospital> findByStateId(Long stateId, Pageable pageable);
    Page<Hospital> findByDistrictId(Long districtId, Pageable pageable);
}
