package com.hackathon.sos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_history", indexes = {
        @Index(name = "idx_user_recorded", columnList = "user_id,recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 200)
    private String address;

    @Column(length = 20)
    private String accuracy;  // GPS 정확도 (meters)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt;
}