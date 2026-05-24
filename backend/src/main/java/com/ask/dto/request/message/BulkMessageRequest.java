package com.ask.dto.request.message;

import com.ask.enums.MessageChannel;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BulkMessageRequest {

    private Long templateId;
    
    private String customText;

    @NotNull(message = "Message channel is required")
    private MessageChannel channel;

    private String targetRole;
    private Long stateId;
    private Long districtId;
    private Long blockId;
    private Long storeId;
}
