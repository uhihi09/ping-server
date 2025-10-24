package com.hackathon.sos.controller;

import com.hackathon.sos.dto.response.ApiResponse;
import com.hackathon.sos.dto.response.LocationHistoryResponse;
import com.hackathon.sos.entity.User;
import com.hackathon.sos.service.LocationService;
import com.hackathon.sos.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;
    private final UserService userService;

    /**
     * 사용자의 최근 위치 히스토리 조회
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LocationHistoryResponse>>> getLocationHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        logger.info("위치 히스토리 조회: username={}, limit={}", userDetails.getUsername(), limit);

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<LocationHistoryResponse> locations = locationService.getUserLocationHistory(
                user.getId(), limit);

        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    /**
     * 최근 24시간 위치 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<LocationHistoryResponse>>> getRecentLocations(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("최근 24시간 위치 조회: username={}", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<LocationHistoryResponse> locations = locationService.getRecentLocations(user.getId());

        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    /**
     * 특정 기간의 위치 히스토리 조회
     */
    @GetMapping("/history/range")
    public ResponseEntity<ApiResponse<List<LocationHistoryResponse>>> getLocationHistoryByDateRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("기간별 위치 히스토리 조회: username={}, start={}, end={}",
                userDetails.getUsername(), startDate, endDate);

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<LocationHistoryResponse> locations = locationService.getLocationHistoryByDateRange(
                user.getId(), startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(locations));
    }
}