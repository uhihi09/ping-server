package com.hackathon.sos.repository;

import com.hackathon.sos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByDeviceId(String deviceId);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByDeviceId(String deviceId);
}