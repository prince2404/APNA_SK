package com.ask.service;

import com.ask.dto.request.billing.BillRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.billing.BillResponse;

public interface BillService {
    BillResponse createBill(BillRequest request, String currentUserEmail);
    BillResponse getBillByNumber(String billNumber, String currentUserEmail);
    BillResponse getBillById(Long id, String currentUserEmail);
    PageResponse<BillResponse> getBills(Long storeId, int page, int size, String currentUserEmail);
    BillResponse cancelBill(Long id, String cancelReason, String currentUserEmail);
    byte[] generateBillPdf(Long id, String currentUserEmail);
}
