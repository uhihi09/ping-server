package com.hackathon.sos.controller;

import com.hackathon.sos.dto.request.EmergencyContactRequest;
import com.hackathon.sos.dto.response.ApiResponse;
import com.hackathon.sos.dto.response.EmergencyContactResponse;
import com.hackathon.sos.entity.User;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("사용자 정보 조회: username={}", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 긴급 연락처 추가
     */
    @PostMapping("/emergency-contacts")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> addEmergencyContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EmergencyContactRequest request) {
        logger.info("긴급 연락처 추가 API 호출: username={}", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        EmergencyContactResponse response = userService.addEmergencyContact(user.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("긴급 연락처가 추가되었습니다", response));
    }

    /**
     * 긴급 연락처 목록 조회
     */
    @GetMapping("/emergency-contacts")
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> getEmergencyContacts(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("긴급 연락처 목록 조회: username={}", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<EmergencyContactResponse> contacts = userService.getEmergencyContacts(user.getId());

        return ResponseEntity.ok(ApiResponse.success(contacts));
    }

    /**
     * 긴급 연락처 수정
     */
    @PutMapping("/emergency-contacts/{contactId}")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> updateEmergencyContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactRequest request) {
        logger.info("긴급 연락처 수정 API 호출: username={}, contactId={}",
                userDetails.getUsername(), contactId);

        User user = userService.getUserByUsername(userDetails.getUsername());
        EmergencyContactResponse response = userService.updateEmergencyContact(
                user.getId(), contactId, request);

        return ResponseEntity.ok(ApiResponse.success("긴급 연락처가 수정되었습니다", response));
    }

    /**
     * 긴급 연락처 삭제
     */
    @DeleteMapping("/emergency-contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long contactId) {
        logger.info("긴급 연락처 삭제 API 호출: username={}, contactId={}",
                userDetails.getUsername(), contactId);

        User user = userService.getUserByUsername(userDetails.getUsername());
        userService.deleteEmergencyContact(user.getId(), contactId);

        return ResponseEntity.ok(ApiResponse.success("긴급 연락처가 삭제되었습니다", null));
    }
}