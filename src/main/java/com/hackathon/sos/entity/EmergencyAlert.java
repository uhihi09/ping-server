package com.hackathon.sos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts", indexes = {
        @Index(name = "idx_user_alert_time", columnList = "user_id,alert_time"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double latitude;  // 위도

    @Column(nullable = false)
    private Double longitude;  // 경도

    @Column(length = 200)
    private String address;  // 주소 (역지오코딩 결과)

    @Column(columnDefinition = "TEXT")
    private String audioTranscript;  // AI가 인식한 음성 텍스트

    @Column(columnDefinition = "TEXT")
    private String situationAnalysis;  // GPT가 분석한 상황

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyType emergencyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmergencyStatus status = EmergencyStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(length = 500)
    private String notificationMessage;  // 전송된 알림 메시지

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime alertTime;

    private LocalDateTime resolvedTime;

    public enum EmergencyType {
        ACCIDENT("사고"),
        ASSAULT("폭행/범죄"),
        KIDNAPPING("납치/유괴"),
        MEDICAL("응급의료"),
        FIRE("화재"),
        NATURAL_DISASTER("재난"),
        STALKING("스토킹"),
        OTHER("기타");

        private final String description;

        EmergencyType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum EmergencyStatus {
        PENDING("대기중"),
        NOTIFIED("알림 발송됨"),
        IN_PROGRESS("처리중"),
        RESOLVED("해결됨"),
        FALSE_ALARM("오작동");

        private final String description;

        EmergencyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}