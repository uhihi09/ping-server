package com.hackathon.sos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String relationship;
    private Integer priority;
    private Boolean active;
    private LocalDateTime createdAt;
}