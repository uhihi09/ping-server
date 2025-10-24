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
public class LocationHistoryResponse {
    private Long id;
    private Long userId;
    private Double latitude;
    private Double longitude;
    private String address;
    private String accuracy;
    private LocalDateTime recordedAt;
}