package com.hackathon.sos.dto.response;

import com.hackathon.sos.entity.EmergencyAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlertResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhoneNumber;
    private Double latitude;
    private Double longitude;
    private String address;
    private String audioTranscript;
    private String situationAnalysis;
    private EmergencyAlert.EmergencyType emergencyType;
    private String emergencyTypeDescription;
    private EmergencyAlert.EmergencyStatus status;
    private String statusDescription;
    private String additionalInfo;
    private Boolean notificationSent;
    private String notificationMessage;
    private LocalDateTime alertTime;
    private LocalDateTime resolvedTime;
}