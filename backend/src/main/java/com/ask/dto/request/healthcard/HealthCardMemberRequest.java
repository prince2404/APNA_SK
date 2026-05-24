package com.ask.dto.request.healthcard;

import com.ask.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HealthCardMemberRequest {

    @NotBlank(message = "Family member name is required")
    private String name;

    @NotBlank(message = "Relation is required")
    private String relation;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age cannot be negative")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;
}
