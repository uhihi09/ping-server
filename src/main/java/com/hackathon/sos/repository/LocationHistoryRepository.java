package com.hackathon.sos.repository;

import com.hackathon.sos.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    List<LocationHistory> findByUserIdOrderByRecordedAtDesc(Long userId);

    List<LocationHistory> findByUserIdAndRecordedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT l FROM LocationHistory l WHERE l.user.id = :userId ORDER BY l.recordedAt DESC LIMIT 10")
    List<LocationHistory> findTop10ByUserIdOrderByRecordedAtDesc(@Param("userId") Long userId);

    @Query("SELECT l FROM LocationHistory l WHERE l.user.id = :userId AND l.recordedAt >= :since ORDER BY l.recordedAt DESC")
    List<LocationHistory> findRecentLocationsByUserId(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );
}