package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.patient.PatientRequest;
import com.ask.dto.response.patient.PatientResponse;
import com.ask.entity.*;
import com.ask.enums.EntityStatus;
import com.ask.enums.Gender;
import com.ask.enums.MessagingPreference;
import com.ask.exception.BusinessRuleException;
import com.ask.mapper.PatientMapper;
import com.ask.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private User receptionist;
    private Role receptionistRole;
    private Store store;
    private Block block;
    private District district;
    private State state;
    private PatientRequest patientRequest;

    @BeforeEach
    void setUp() {
        state = State.builder().id(1L).name("Bihar").status(EntityStatus.ACTIVE).build();
        
        district = District.builder().id(1L).name("Patna").state(state).status(EntityStatus.ACTIVE).build();
        
        block = Block.builder().id(1L).name("Block A").district(district).status(EntityStatus.ACTIVE).build();
        
        store = Store.builder().id(1L).name("Store A").code("ST01").block(block).status(EntityStatus.ACTIVE).build();

        receptionistRole = Role.builder().id(6L).name(RoleConstants.RECEPTIONIST).build();

        receptionist = User.builder()
                .id(1L)
                .email("receptionist@ask.com")
                .role(receptionistRole)
                .store(store)
                .build();

        patientRequest = PatientRequest.builder()
                .fullName("John Doe")
                .age(30)
                .gender(Gender.MALE)
                .phone("9876543210")
                .email("john@gmail.com")
                .address("Patna, Bihar")
                .stateId(1L)
                .districtId(1L)
                .blockId(1L)
                .storeId(1L)
                .messagingPref(MessagingPreference.ALL)
                .build();
    }

    @Test
    void registerPatient_Success() {
        when(userRepository.findByEmail("receptionist@ask.com")).thenReturn(Optional.of(receptionist));
        when(stateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(1L)).thenReturn(Optional.of(district));
        when(blockRepository.findById(1L)).thenReturn(Optional.of(block));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(patientRepository.existsByPhone("9876543210")).thenReturn(false);

        Patient patient = Patient.builder()
                .id(1L)
                .fullName("John Doe")
                .phone("9876543210")
                .build();
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponse response = PatientResponse.builder().id(1L).fullName("John Doe").phone("9876543210").build();
        when(patientMapper.toPatientResponse(any(Patient.class))).thenReturn(response);

        PatientResponse res = patientService.registerPatient(patientRequest, "receptionist@ask.com");

        assertNotNull(res);
        assertEquals("John Doe", res.getFullName());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void registerPatient_DuplicatePhone_ThrowsException() {
        when(userRepository.findByEmail("receptionist@ask.com")).thenReturn(Optional.of(receptionist));
        when(stateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(1L)).thenReturn(Optional.of(district));
        when(blockRepository.findById(1L)).thenReturn(Optional.of(block));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(patientRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> 
            patientService.registerPatient(patientRequest, "receptionist@ask.com")
        );
    }

    @Test
    void bulkUploadPatients_Success() {
        when(userRepository.findByEmail("receptionist@ask.com")).thenReturn(Optional.of(receptionist));
        when(patientRepository.existsByPhone("9999999999")).thenReturn(false);
        when(patientRepository.existsByPhone("8888888888")).thenReturn(true); // duplicate in DB

        String csvData = "full_name,age,gender,phone,email,address,messaging_pref\n" +
                "Alice,25,FEMALE,9999999999,alice@gmail.com,Address 1,ALL\n" +
                "Bob,30,MALE,8888888888,bob@gmail.com,Address 2,ALL\n" + // skipped (duplicate DB)
                "Charlie,35,MALE,9999999999,charlie@gmail.com,Address 3,ALL\n" + // skipped (duplicate CSV)
                "InvalidRow,,INVALID,123,,,\n"; // invalid format

        MockMultipartFile file = new MockMultipartFile("file", "patients.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> result = patientService.bulkUploadPatients(file, "receptionist@ask.com");

        assertNotNull(result);
        assertEquals(4, result.get("total"));
        assertEquals(1, result.get("success"));
        assertEquals(2, result.get("skipped")); // Bob (DB dup) and Charlie (CSV dup)
        assertEquals(1, result.get("invalid"));
    }
}
