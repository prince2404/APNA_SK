package com.ask.dto.request.healthcard;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HealthCardRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;
}
