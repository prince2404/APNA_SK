package com.ask.dto.response.healthcard;

import com.ask.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HealthCardMemberResponse {
    private Long id;
    private String name;
    private String relation;
    private Integer age;
    private Gender gender;
    private LocalDateTime createdAt;
}
