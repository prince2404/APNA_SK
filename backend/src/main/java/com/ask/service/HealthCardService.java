package com.ask.service;

import com.ask.dto.request.healthcard.HealthCardMemberRequest;
import com.ask.dto.request.healthcard.HealthCardRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.healthcard.HealthCardMemberResponse;
import com.ask.dto.response.healthcard.HealthCardResponse;

public interface HealthCardService {
    HealthCardResponse issueHealthCard(HealthCardRequest request, String currentUserEmail);
    HealthCardResponse getHealthCardByNumber(String cardNumber, String currentUserEmail);
    HealthCardResponse getHealthCardByPatientId(Long patientId, String currentUserEmail);
    HealthCardResponse addFamilyMember(Long cardId, HealthCardMemberRequest request, String currentUserEmail);
    void removeFamilyMember(Long cardId, Long memberId, String currentUserEmail);
    PageResponse<HealthCardResponse> getHealthCards(int page, int size, String currentUserEmail);
}
