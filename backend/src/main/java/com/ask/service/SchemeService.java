package com.ask.service;

import com.ask.dto.request.billing.SchemeRequest;
import com.ask.dto.response.billing.SchemeResponse;
import java.util.List;

public interface SchemeService {
    SchemeResponse createScheme(SchemeRequest request, String currentUserEmail);
    List<SchemeResponse> getSchemes(String currentUserEmail);
    void toggleSchemeStatus(Long id, String currentUserEmail);
}
