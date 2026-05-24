package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.patient.PatientRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.patient.PatientResponse;
import com.ask.entity.*;
import com.ask.enums.EntityStatus;
import com.ask.enums.Gender;
import com.ask.enums.MessagingPreference;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.PatientMapper;
import com.ask.repository.*;
import com.ask.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final StoreRepository storeRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientMapper patientMapper;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void validateGeographicAlignment(Long stateId, Long districtId, Long blockId, Long storeId) {
        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", stateId));
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", districtId));
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", blockId));

        if (!district.getState().getId().equals(state.getId())) {
            throw new BusinessRuleException("District does not belong to the selected state");
        }
        if (!block.getDistrict().getId().equals(district.getId())) {
            throw new BusinessRuleException("Block does not belong to the selected district");
        }
        if (storeId != null) {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
            if (!store.getBlock().getId().equals(block.getId())) {
                throw new BusinessRuleException("Store does not belong to the selected block");
            }
        }
    }

    private void ensureGeographicScope(User user, Long stateId, Long districtId, Long blockId, Long storeId) {
        String roleName = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName)) {
            return;
        }
        if (user.getState() != null && !user.getState().getId().equals(stateId)) {
            throw new GeographicScopeException("State is outside user's assigned scope");
        }
        if (user.getDistrict() != null && !user.getDistrict().getId().equals(districtId)) {
            throw new GeographicScopeException("District is outside user's assigned scope");
        }
        if (user.getBlock() != null && !user.getBlock().getId().equals(blockId)) {
            throw new GeographicScopeException("Block is outside user's assigned scope");
        }
        if (user.getStore() != null && (storeId == null || !user.getStore().getId().equals(storeId))) {
            throw new GeographicScopeException("Store is outside user's assigned scope");
        }
    }

    private void validateStoreScope(User user, Long requestedStoreId) {
        if (requestedStoreId == null) return;
        Store store = storeRepository.findById(requestedStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", requestedStoreId));
        String role = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            return;
        }
        if (user.getState() != null && !store.getBlock().getDistrict().getState().getId().equals(user.getState().getId())) {
            throw new GeographicScopeException("Store is outside your state scope");
        }
        if (user.getDistrict() != null && !store.getBlock().getDistrict().getId().equals(user.getDistrict().getId())) {
            throw new GeographicScopeException("Store is outside your district scope");
        }
        if (user.getBlock() != null && !store.getBlock().getId().equals(user.getBlock().getId())) {
            throw new GeographicScopeException("Store is outside your block scope");
        }
        if (user.getStore() != null && !store.getId().equals(user.getStore().getId())) {
            throw new GeographicScopeException("Store is outside your store scope");
        }
    }

    @Override
    @Transactional
    public PatientResponse registerPatient(PatientRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        validateGeographicAlignment(request.getStateId(), request.getDistrictId(), request.getBlockId(), request.getStoreId());
        ensureGeographicScope(currentUser, request.getStateId(), request.getDistrictId(), request.getBlockId(), request.getStoreId());

        if (patientRepository.existsByPhone(request.getPhone())) {
            throw new BusinessRuleException("A patient with this phone number already exists");
        }

        State state = stateRepository.getReferenceById(request.getStateId());
        District district = districtRepository.getReferenceById(request.getDistrictId());
        Block block = blockRepository.getReferenceById(request.getBlockId());
        Store store = request.getStoreId() != null ? storeRepository.getReferenceById(request.getStoreId()) : null;
        Hospital hospital = request.getHospitalId() != null ? hospitalRepository.getReferenceById(request.getHospitalId()) : null;

        Patient patient = Patient.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .state(state)
                .district(district)
                .block(block)
                .store(store)
                .hospital(hospital)
                .messagingPref(request.getMessagingPref())
                .status(EntityStatus.ACTIVE)
                .createdBy(currentUser)
                .build();

        Patient saved = patientRepository.save(patient);
        return patientMapper.toPatientResponse(saved);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        ensureGeographicScope(currentUser, patient.getState().getId(), patient.getDistrict().getId(), patient.getBlock().getId(), 
                patient.getStore() != null ? patient.getStore().getId() : null);

        validateGeographicAlignment(request.getStateId(), request.getDistrictId(), request.getBlockId(), request.getStoreId());
        ensureGeographicScope(currentUser, request.getStateId(), request.getDistrictId(), request.getBlockId(), request.getStoreId());

        Optional<Patient> existingWithPhone = patientRepository.findByPhone(request.getPhone());
        if (existingWithPhone.isPresent() && !existingWithPhone.get().getId().equals(id)) {
            throw new BusinessRuleException("Another patient with this phone number already exists");
        }

        State state = stateRepository.getReferenceById(request.getStateId());
        District district = districtRepository.getReferenceById(request.getDistrictId());
        Block block = blockRepository.getReferenceById(request.getBlockId());
        Store store = request.getStoreId() != null ? storeRepository.getReferenceById(request.getStoreId()) : null;
        Hospital hospital = request.getHospitalId() != null ? hospitalRepository.getReferenceById(request.getHospitalId()) : null;

        patient.setFullName(request.getFullName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setState(state);
        patient.setDistrict(district);
        patient.setBlock(block);
        patient.setStore(store);
        patient.setHospital(hospital);
        patient.setMessagingPref(request.getMessagingPref());

        Patient saved = patientRepository.save(patient);
        return patientMapper.toPatientResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> getPatients(String search, Long storeId, int page, int size, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        validateStoreScope(currentUser, storeId);

        String role = currentUser.getRole().getName();
        Long sId = null;
        Long dId = null;
        Long bId = null;
        Long stId = null;

        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            stId = storeId;
        } else if (RoleConstants.STATE_ADMIN.equals(role)) {
            sId = currentUser.getState().getId();
            stId = storeId;
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            dId = currentUser.getDistrict().getId();
            stId = storeId;
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            bId = currentUser.getBlock().getId();
            stId = storeId;
        } else {
            stId = currentUser.getStore().getId();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        
        final Long finalStateId = sId;
        final Long finalDistrictId = dId;
        final Long finalBlockId = bId;
        final Long finalStoreId = stId;

        Specification<Patient> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate phonePredicate = cb.like(root.get("phone"), pattern);
                predicates.add(cb.or(namePredicate, phonePredicate));
            }
            if (finalStoreId != null) {
                predicates.add(cb.equal(root.get("store").get("id"), finalStoreId));
            }
            if (finalBlockId != null) {
                predicates.add(cb.equal(root.get("block").get("id"), finalBlockId));
            }
            if (finalDistrictId != null) {
                predicates.add(cb.equal(root.get("district").get("id"), finalDistrictId));
            }
            if (finalStateId != null) {
                predicates.add(cb.equal(root.get("state").get("id"), finalStateId));
            }
            predicates.add(cb.equal(root.get("status"), EntityStatus.ACTIVE));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Patient> patientPage = patientRepository.findAll(spec, pageable);
        List<PatientResponse> content = patientPage.getContent().stream()
                .map(patientMapper::toPatientResponse)
                .toList();

        return PageResponse.of(patientPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        ensureGeographicScope(currentUser, patient.getState().getId(), patient.getDistrict().getId(), patient.getBlock().getId(), 
                patient.getStore() != null ? patient.getStore().getId() : null);

        return patientMapper.toPatientResponse(patient);
    }

    @Override
    @Transactional
    public Map<String, Object> bulkUploadPatients(MultipartFile file, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        
        // Check if the user is scoped to a block or store
        Store store = currentUser.getStore();
        Block block = currentUser.getBlock();
        
        if (store == null && block == null) {
            throw new BusinessRuleException("You must be assigned to a Block or Store to bulk upload patients.");
        }

        State targetState;
        District targetDistrict;
        Block targetBlock;
        Store targetStore;

        if (store != null) {
            targetStore = store;
            targetBlock = store.getBlock();
            targetDistrict = targetBlock.getDistrict();
            targetState = targetDistrict.getState();
        } else {
            targetStore = null;
            targetBlock = block;
            targetDistrict = block.getDistrict();
            targetState = targetDistrict.getState();
        }

        int successCount = 0;
        int skippedDuplicateCount = 0;
        int invalidCount = 0;
        int totalRows = 0;

        Set<String> processedPhoneNumbers = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new BusinessRuleException("Uploaded CSV is empty");
            }

            List<String> headers = parseCsvLine(headerLine);
            int nameIdx = -1, ageIdx = -1, genderIdx = -1, phoneIdx = -1, emailIdx = -1, addressIdx = -1, prefIdx = -1;

            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i).toLowerCase().trim().replace("\"", "").replace("_", " ");
                if (h.contains("name") || h.contains("full name")) nameIdx = i;
                else if (h.contains("age")) ageIdx = i;
                else if (h.contains("gender")) genderIdx = i;
                else if (h.contains("phone") || h.contains("mobile") || h.contains("contact")) phoneIdx = i;
                else if (h.contains("email")) emailIdx = i;
                else if (h.contains("address")) addressIdx = i;
                else if (h.contains("messaging pref") || h.contains("pref")) prefIdx = i;
            }

            if (nameIdx == -1 || phoneIdx == -1 || genderIdx == -1) {
                throw new BusinessRuleException("CSV must contain 'name', 'phone', and 'gender' columns");
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                totalRows++;
                List<String> values = parseCsvLine(line);

                if (values.size() <= Math.max(Math.max(nameIdx, phoneIdx), genderIdx)) {
                    invalidCount++;
                    continue;
                }

                String fullName = values.get(nameIdx).trim();
                String phone = values.get(phoneIdx).trim();
                String genderStr = values.get(genderIdx).trim().toUpperCase();

                if (fullName.isEmpty() || phone.isEmpty() || genderStr.isEmpty()) {
                    invalidCount++;
                    continue;
                }

                // Phone format validation (10 to 12 digits)
                if (!phone.matches("^[0-9]{10,12}$")) {
                    invalidCount++;
                    continue;
                }

                // Gender parsing
                Gender gender;
                try {
                    gender = Gender.valueOf(genderStr);
                } catch (IllegalArgumentException e) {
                    invalidCount++;
                    continue;
                }

                // Duplicate checking
                if (processedPhoneNumbers.contains(phone) || patientRepository.existsByPhone(phone)) {
                    skippedDuplicateCount++;
                    continue;
                }

                Integer age = null;
                if (ageIdx != -1 && ageIdx < values.size()) {
                    try {
                        age = Integer.parseInt(values.get(ageIdx).trim());
                    } catch (NumberFormatException ignored) {}
                }

                String email = (emailIdx != -1 && emailIdx < values.size()) ? values.get(emailIdx).trim() : null;
                String address = (addressIdx != -1 && addressIdx < values.size()) ? values.get(addressIdx).trim() : null;

                MessagingPreference pref = MessagingPreference.ALL;
                if (prefIdx != -1 && prefIdx < values.size()) {
                    try {
                        pref = MessagingPreference.valueOf(values.get(prefIdx).trim().toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                Patient patient = Patient.builder()
                        .fullName(fullName)
                        .age(age)
                        .gender(gender)
                        .phone(phone)
                        .email(email)
                        .address(address)
                        .state(targetState)
                        .district(targetDistrict)
                        .block(targetBlock)
                        .store(targetStore)
                        .messagingPref(pref)
                        .status(EntityStatus.ACTIVE)
                        .createdBy(currentUser)
                        .build();

                patientRepository.save(patient);
                processedPhoneNumbers.add(phone);
                successCount++;
            }
        } catch (Exception e) {
            log.error("Failed to parse bulk patient upload file", e);
            throw new BusinessRuleException("Error occurred while parsing CSV file: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", totalRows);
        result.put("success", successCount);
        result.put("skipped", skippedDuplicateCount);
        result.put("invalid", invalidCount);
        return result;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());
        return values;
    }
}
