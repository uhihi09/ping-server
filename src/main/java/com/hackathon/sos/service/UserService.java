package com.hackathon.sos.service;

import com.hackathon.sos.dto.request.EmergencyContactRequest;
import com.hackathon.sos.dto.response.EmergencyContactResponse;
import com.hackathon.sos.entity.EmergencyContact;
import com.hackathon.sos.entity.User;
import com.hackathon.sos.exception.ResourceNotFoundException;
import com.hackathon.sos.repository.EmergencyContactRepository;
import com.hackathon.sos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final EmergencyContactRepository emergencyContactRepository;

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional
    public EmergencyContactResponse addEmergencyContact(Long userId, EmergencyContactRequest request) {
        logger.info("긴급 연락처 추가: userId={}, name={}", userId, request.getName());

        User user = getUserById(userId);

        EmergencyContact contact = EmergencyContact.builder()
                .user(user)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .relationship(request.getRelationship())
                .priority(request.getPriority())
                .active(true)
                .build();

        EmergencyContact savedContact = emergencyContactRepository.save(contact);
        logger.info("긴급 연락처 추가 완료: contactId={}", savedContact.getId());

        return convertToResponse(savedContact);
    }

    @Transactional(readOnly = true)
    public List<EmergencyContactResponse> getEmergencyContacts(Long userId) {
        logger.info("긴급 연락처 목록 조회: userId={}", userId);

        List<EmergencyContact> contacts = emergencyContactRepository
                .findByUserIdAndActiveTrueOrderByPriorityAsc(userId);

        return contacts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmergencyContactResponse updateEmergencyContact(Long userId, Long contactId, EmergencyContactRequest request) {
        logger.info("긴급 연락처 수정: userId={}, contactId={}", userId, contactId);

        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", "id", contactId));

        if (!contact.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 긴급 연락처를 수정할 권한이 없습니다");
        }

        contact.setName(request.getName());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setEmail(request.getEmail());
        contact.setRelationship(request.getRelationship());
        contact.setPriority(request.getPriority());

        EmergencyContact updatedContact = emergencyContactRepository.save(contact);
        logger.info("긴급 연락처 수정 완료: contactId={}", updatedContact.getId());

        return convertToResponse(updatedContact);
    }

    @Transactional
    public void deleteEmergencyContact(Long userId, Long contactId) {
        logger.info("긴급 연락처 삭제: userId={}, contactId={}", userId, contactId);

        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", "id", contactId));

        if (!contact.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 긴급 연락처를 삭제할 권한이 없습니다");
        }

        contact.setActive(false);
        emergencyContactRepository.save(contact);
        logger.info("긴급 연락처 삭제 완료: contactId={}", contactId);
    }

    private EmergencyContactResponse convertToResponse(EmergencyContact contact) {
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .phoneNumber(contact.getPhoneNumber())
                .email(contact.getEmail())
                .relationship(contact.getRelationship())
                .priority(contact.getPriority())
                .active(contact.getActive())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}