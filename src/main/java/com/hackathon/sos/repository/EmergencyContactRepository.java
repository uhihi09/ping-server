package com.hackathon.sos.repository;

import com.hackathon.sos.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserIdAndActiveTrue(Long userId);
    List<EmergencyContact> findByUserIdOrderByPriorityAsc(Long userId);
    List<EmergencyContact> findByUserIdAndActiveTrueOrderByPriorityAsc(Long userId);
}