package com.ask.dto.response.message;

import com.ask.enums.MessageChannel;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageTemplateResponse {
    private Long id;
    private String name;
    private MessageChannel channel;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
