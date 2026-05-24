package com.ask.service;

import com.ask.dto.request.patient.PatientRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.patient.PatientResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface PatientService {
    PatientResponse registerPatient(PatientRequest request, String currentUserEmail);
    PatientResponse updatePatient(Long id, PatientRequest request, String currentUserEmail);
    PageResponse<PatientResponse> getPatients(String search, Long storeId, int page, int size, String currentUserEmail);
    PatientResponse getPatientById(Long id, String currentUserEmail);
    Map<String, Object> bulkUploadPatients(MultipartFile file, String currentUserEmail);
}
