package com.ask.service;

import com.ask.dto.request.message.BulkMessageRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.message.BulkMessageLogResponse;

public interface BulkMessageService {
    BulkMessageLogResponse sendBulkMessage(BulkMessageRequest request, String senderEmail);
    PageResponse<BulkMessageLogResponse> getBulkMessageHistory(int page, int size, String senderEmail);
}
