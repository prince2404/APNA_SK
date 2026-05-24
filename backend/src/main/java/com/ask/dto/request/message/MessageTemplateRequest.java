package com.ask.dto.request.message;

import com.ask.enums.MessageChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Channel is required")
    private MessageChannel channel;

    @NotBlank(message = "Content is required")
    private String content;
}
