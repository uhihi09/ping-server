package com.hackathon.sos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlertRequest {

    @NotBlank(message = "장치 ID는 필수입니다")
    private String deviceId;

    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    private Double longitude;

    private String audioData;  // Base64 인코딩된 오디오 데이터 또는 URL

    private String audioTranscript;  // 이미 변환된 텍스트 (옵션)

    private String additionalInfo;  // 추가 정보
}