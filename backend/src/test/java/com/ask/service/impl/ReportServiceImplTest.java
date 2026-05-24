package com.ask.service.impl;

import com.ask.entity.*;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User user(Long id, String email, String roleName) {
        Role role = Role.builder().id(id).name(roleName).displayName(roleName).build();
        return User.builder()
                .id(id)
                .fullName("Test User " + id)
                .email(email)
                .role(role)
                .build();
    }

    @Test
    void getSalesReportGeneratesCorrectMappedOutput() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        TypedQuery<Object[]> queryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(queryMock);

        LocalDateTime now = LocalDateTime.now();
        Object[] row = new Object[]{
                now,
                "Store A",
                "ST001",
                5L,
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(4.5),
                BigDecimal.valueOf(94.5)
        };
        List<Object[]> rowsList = new ArrayList<>();
        rowsList.add(row);
        when(queryMock.getResultList()).thenReturn(rowsList);

        List<Map<String, Object>> result = reportService.getSalesReport(null, null, null, superAdmin.getEmail());

        assertEquals(1, result.size());
        Map<String, Object> map = result.get(0);
        assertEquals(now.toLocalDate().toString(), map.get("date"));
        assertEquals("Store A", map.get("storeName"));
        assertEquals(5L, map.get("totalBills"));
        assertEquals(BigDecimal.valueOf(94.5), map.get("netAmount"));
    }

    @Test
    void getUserActivityReportThrowsAccessDeniedForNonAdmin() {
        User receptionist = user(2L, "receptionist@askhealth.in", "RECEPTIONIST");
        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));

        assertThrows(AccessDeniedException.class, () ->
                reportService.getUserActivityReport(null, null, null, null, receptionist.getEmail())
        );
    }

    @Test
    void exportToCsvFormatsHeadersAndValuesCorrectly() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", "John, Doe");
        row.put("value", 123);
        data.add(row);

        List<String> headers = List.of("Name", "Value");
        List<String> keys = List.of("name", "value");

        byte[] csvBytes = reportService.exportToCsv(data, headers, keys);
        String csv = new String(csvBytes);

        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);
        assertEquals("Name,Value", lines[0]);
        assertEquals("\"John, Doe\",123", lines[1]);
    }
}
