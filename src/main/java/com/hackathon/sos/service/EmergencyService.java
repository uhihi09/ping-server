package com.hackathon.sos.service;

import com.hackathon.sos.dto.request.EmergencyAlertRequest;
import com.hackathon.sos.dto.response.EmergencyAlertResponse;
import com.hackathon.sos.entity.EmergencyAlert;
import com.hackathon.sos.entity.EmergencyContact;
import com.hackathon.sos.entity.User;
import com.hackathon.sos.exception.ResourceNotFoundException;
import com.hackathon.sos.repository.EmergencyAlertRepository;
import com.hackathon.sos.repository.EmergencyContactRepository;
import com.hackathon.sos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyService.class);

    private final UserRepository userRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final AIAnalysisService aiAnalysisService;
    private final NotificationService notificationService;
    private final LocationService locationService;

    /**
     * 긴급 상황 알림 생성 (아두이노/라즈베리파이에서 호출)
     */
    @Transactional
    public EmergencyAlertResponse createEmergencyAlert(EmergencyAlertRequest request) {
        logger.info("긴급 알림 생성: deviceId={}", request.getDeviceId());

        // 장치 ID로 사용자 찾기
        User user = userRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("등록되지 않은 장치입니다: " + request.getDeviceId()));

        // AI를 통한 상황 분석
        String situationAnalysis = "분석중...";
        String emergencyTypeStr = "OTHER";

        if (request.getAudioTranscript() != null && !request.getAudioTranscript().trim().isEmpty()) {
            situationAnalysis = aiAnalysisService.analyzeEmergencySituation(request.getAudioTranscript());
            emergencyTypeStr = aiAnalysisService.determineEmergencyType(situationAnalysis);
        } else {
            situationAnalysis = "음성 인식 실패 - 긴급 버튼이 눌렸습니다. 즉시 확인이 필요합니다.";
        }

        // 주소 조회 (역지오코딩)
        String address = locationService.reverseGeocode(request.getLatitude(), request.getLongitude());

        // 긴급 알림 생성
        EmergencyAlert alert = EmergencyAlert.builder()
                .user(user)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(address)
                .audioTranscript(request.getAudioTranscript())
                .situationAnalysis(situationAnalysis)
                .emergencyType(EmergencyAlert.EmergencyType.valueOf(emergencyTypeStr))
                .status(EmergencyAlert.EmergencyStatus.PENDING)
                .additionalInfo(request.getAdditionalInfo())
                .notificationSent(false)
                .build();

        EmergencyAlert savedAlert = emergencyAlertRepository.save(alert);
        logger.info("긴급 알림 저장 완료: alertId={}", savedAlert.getId());

        // 위치 히스토리 저장
        locationService.saveLocationHistory(user, request.getLatitude(), request.getLongitude(), address);

        // 비동기로 알림 전송
        sendEmergencyNotifications(savedAlert);

        return convertToResponse(savedAlert);
    }

    /**
     * 긴급 연락처에게 알림 전송 (비동기)
     */
    @Async
    public void sendEmergencyNotifications(EmergencyAlert alert) {
        logger.info("긴급 알림 전송 시작: alertId={}", alert.getId());

        try {
            // 긴급 연락처 목록 조회
            List<EmergencyContact> contacts = emergencyContactRepository
                    .findByUserIdAndActiveTrueOrderByPriorityAsc(alert.getUser().getId());

            if (contacts.isEmpty()) {
                logger.warn("긴급 연락처가 등록되어 있지 않습니다: userId={}", alert.getUser().getId());
                updateAlertStatus(alert.getId(), EmergencyAlert.EmergencyStatus.NOTIFIED,
                        "긴급 연락처 미등록 - 알림 전송 실패");
                return;
            }

            // 알림 메시지 생성
            String message = createNotificationMessage(alert);

            // 각 연락처에게 알림 전송
            int successCount = 0;
            for (EmergencyContact contact : contacts) {
                try {
                    // SMS 전송
                    if (contact.getPhoneNumber() != null) {
                        notificationService.sendSMS(contact.getPhoneNumber(), message);
                        successCount++;
                    }

                    // 이메일 전송 (선택적)
                    if (contact.getEmail() != null) {
                        notificationService.sendEmail(contact.getEmail(),
                                "🚨 긴급 상황 알림", message);
                    }
                } catch (Exception e) {
                    logger.error("알림 전송 실패: contactId={}, error={}", contact.getId(), e.getMessage());
                }
            }

            // 알림 전송 상태 업데이트
            updateAlertStatus(alert.getId(), EmergencyAlert.EmergencyStatus.NOTIFIED,
                    successCount + "명의 긴급 연락처에게 알림 전송 완료");

            logger.info("긴급 알림 전송 완료: alertId={}, 전송 성공={}/{}명",
                    alert.getId(), successCount, contacts.size());

        } catch (Exception e) {
            logger.error("긴급 알림 전송 중 오류: alertId={}, error={}", alert.getId(), e.getMessage(), e);
            updateAlertStatus(alert.getId(), EmergencyAlert.EmergencyStatus.PENDING,
                    "알림 전송 중 오류 발생");
        }
    }

    /**
     * 알림 메시지 생성
     */
    private String createNotificationMessage(EmergencyAlert alert) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 긴급 상황 알림 🚨\n\n");
        message.append("📍 요구조자: ").append(alert.getUser().getName()).append("\n");
        message.append("📞 연락처: ").append(alert.getUser().getPhoneNumber()).append("\n\n");
        message.append("⚠️ 상황: ").append(alert.getEmergencyType().getDescription()).append("\n");
        message.append("🔍 AI 분석:\n").append(alert.getSituationAnalysis()).append("\n\n");
        message.append("📍 위치: ").append(alert.getAddress()).append("\n");
        message.append("🗺️ 좌표: ").append(alert.getLatitude()).append(", ").append(alert.getLongitude()).append("\n");
        message.append("🔗 지도: https://maps.google.com/?q=")
                .append(alert.getLatitude()).append(",").append(alert.getLongitude()).append("\n\n");
        message.append("⏰ 발생 시각: ").append(alert.getAlertTime()).append("\n\n");
        message.append("즉시 확인하시고 필요시 관계 기관(경찰 112, 소방 119)에 신고해주세요!");

        return message.toString();
    }

    /**
     * 알림 상태 업데이트
     */
    @Transactional
    public void updateAlertStatus(Long alertId, EmergencyAlert.EmergencyStatus status, String message) {
        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyAlert", "id", alertId));

        alert.setStatus(status);
        alert.setNotificationSent(status == EmergencyAlert.EmergencyStatus.NOTIFIED ||
                status == EmergencyAlert.EmergencyStatus.IN_PROGRESS);
        alert.setNotificationMessage(message);

        if (status == EmergencyAlert.EmergencyStatus.RESOLVED) {
            alert.setResolvedTime(LocalDateTime.now());
        }

        emergencyAlertRepository.save(alert);
    }

    /**
     * 사용자의 긴급 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<EmergencyAlertResponse> getUserEmergencyAlerts(Long userId) {
        logger.info("긴급 알림 목록 조회: userId={}", userId);

        List<EmergencyAlert> alerts = emergencyAlertRepository.findByUserIdOrderByAlertTimeDesc(userId);

        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 긴급 알림 상세 조회
     */
    @Transactional(readOnly = true)
    public EmergencyAlertResponse getEmergencyAlertById(Long alertId) {
        logger.info("긴급 알림 상세 조회: alertId={}", alertId);

        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyAlert", "id", alertId));

        return convertToResponse(alert);
    }

    /**
     * 긴급 알림 해결 처리
     */
    @Transactional
    public EmergencyAlertResponse resolveEmergencyAlert(Long userId, Long alertId) {
        logger.info("긴급 알림 해결 처리: userId={}, alertId={}", userId, alertId);

        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyAlert", "id", alertId));

        if (!alert.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 알림을 처리할 권한이 없습니다");
        }

        alert.setStatus(EmergencyAlert.EmergencyStatus.RESOLVED);
        alert.setResolvedTime(LocalDateTime.now());

        EmergencyAlert updatedAlert = emergencyAlertRepository.save(alert);
        logger.info("긴급 알림 해결 완료: alertId={}", alertId);

        return convertToResponse(updatedAlert);
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private EmergencyAlertResponse convertToResponse(EmergencyAlert alert) {
        return EmergencyAlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUser().getId())
                .userName(alert.getUser().getName())
                .userPhoneNumber(alert.getUser().getPhoneNumber())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .address(alert.getAddress())
                .audioTranscript(alert.getAudioTranscript())
                .situationAnalysis(alert.getSituationAnalysis())
                .emergencyType(alert.getEmergencyType())
                .emergencyTypeDescription(alert.getEmergencyType().getDescription())
                .status(alert.getStatus())
                .statusDescription(alert.getStatus().getDescription())
                .additionalInfo(alert.getAdditionalInfo())
                .notificationSent(alert.getNotificationSent())
                .notificationMessage(alert.getNotificationMessage())
                .alertTime(alert.getAlertTime())
                .resolvedTime(alert.getResolvedTime())
                .build();
    }
}