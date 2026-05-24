package com.ask.service;

import com.ask.dto.request.hospital.HospitalRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.hospital.HospitalResponse;

public interface HospitalService {
    HospitalResponse createHospital(HospitalRequest request, String currentUserEmail);
    HospitalResponse updateHospital(Long id, HospitalRequest request, String currentUserEmail);
    PageResponse<HospitalResponse> getHospitals(Long stateId, Long districtId, int page, int size, String currentUserEmail);
    HospitalResponse getHospitalById(Long id, String currentUserEmail);
    void toggleHospitalStatus(Long id, String currentUserEmail);
}
