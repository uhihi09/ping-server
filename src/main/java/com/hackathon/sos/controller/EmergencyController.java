package com.hackathon.sos.controller;

import com.hackathon.sos.dto.request.EmergencyAlertRequest;
import com.hackathon.sos.dto.response.ApiResponse;
import com.hackathon.sos.dto.response.EmergencyAlertResponse;
import com.hackathon.sos.entity.User;
import com.hackathon.sos.service.EmergencyService;
import com.hackathon.sos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyController.class);

    private final EmergencyService emergencyService;
    private final UserService userService;

    /**
     * 긴급 알림 생성 (아두이노/라즈베리파이에서 호출)
     * 인증 불필요 - deviceId로 사용자 식별
     */
    @PostMapping("/alert")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> createEmergencyAlert(
            @Valid @RequestBody EmergencyAlertRequest request) {
        logger.info("긴급 알림 API 호출: deviceId={}", request.getDeviceId());

        EmergencyAlertResponse response = emergencyService.createEmergencyAlert(request);

        return ResponseEntity.ok(ApiResponse.success(
                "긴급 알림이 생성되었습니다. 긴급 연락처에 알림을 전송하고 있습니다.", response));
    }

    /**
     * 사용자의 긴급 알림 목록 조회
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getEmergencyAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("긴급 알림 목록 조회: username={}", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<EmergencyAlertResponse> alerts = emergencyService.getUserEmergencyAlerts(user.getId());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * 긴급 알림 상세 조회
     */
    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> getEmergencyAlertById(
            @PathVariable Long alertId) {
        logger.info("긴급 알림 상세 조회: alertId={}", alertId);

        EmergencyAlertResponse response = emergencyService.getEmergencyAlertById(alertId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 긴급 알림 해결 처리
     */
    @PatchMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> resolveEmergencyAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long alertId) {
        logger.info("긴급 알림 해결 처리: username={}, alertId={}",
                userDetails.getUsername(), alertId);

        User user = userService.getUserByUsername(userDetails.getUsername());
        EmergencyAlertResponse response = emergencyService.resolveEmergencyAlert(
                user.getId(), alertId);

        return ResponseEntity.ok(ApiResponse.success("긴급 알림이 해결 처리되었습니다", response));
    }
}