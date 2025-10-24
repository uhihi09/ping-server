package com.hackathon.sos.repository;

import com.hackathon.sos.entity.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByUserIdOrderByAlertTimeDesc(Long userId);

    List<EmergencyAlert> findByUserIdAndAlertTimeBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<EmergencyAlert> findByStatus(EmergencyAlert.EmergencyStatus status);

    @Query("SELECT e FROM EmergencyAlert e WHERE e.user.id = :userId ORDER BY e.alertTime DESC")
    List<EmergencyAlert> findRecentAlertsByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM EmergencyAlert e WHERE e.user.id = :userId AND e.status = :status ORDER BY e.alertTime DESC")
    List<EmergencyAlert> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") EmergencyAlert.EmergencyStatus status
    );
}