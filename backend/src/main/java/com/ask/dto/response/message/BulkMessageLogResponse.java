package com.ask.dto.response.message;

import com.ask.enums.BulkMessageStatus;
import com.ask.enums.MessageChannel;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BulkMessageLogResponse {
    private Long id;
    private String senderName;
    private MessageChannel channel;
    private String targetCriteria;
    private String content;
    private Integer sentCount;
    private BulkMessageStatus status;
    private LocalDateTime createdAt;
}
