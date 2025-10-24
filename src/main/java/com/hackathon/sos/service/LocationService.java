package com.hackathon.sos.service;

import com.hackathon.sos.dto.response.LocationHistoryResponse;
import com.hackathon.sos.entity.LocationHistory;
import com.hackathon.sos.entity.User;
import com.hackathon.sos.repository.LocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationHistoryRepository locationHistoryRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * 위치 히스토리 저장
     */
    @Transactional
    public LocationHistory saveLocationHistory(User user, Double latitude, Double longitude, String address) {
        logger.info("위치 히스토리 저장: userId={}, lat={}, lng={}", user.getId(), latitude, longitude);

        LocationHistory location = LocationHistory.builder()
                .user(user)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .accuracy("GPS")
                .build();

        return locationHistoryRepository.save(location);
    }

    /**
     * 사용자의 최근 위치 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<LocationHistoryResponse> getUserLocationHistory(Long userId, int limit) {
        logger.info("위치 히스토리 조회: userId={}, limit={}", userId, limit);

        List<LocationHistory> locations;
        if (limit > 0) {
            locations = locationHistoryRepository.findTop10ByUserIdOrderByRecordedAtDesc(userId);
        } else {
            locations = locationHistoryRepository.findByUserIdOrderByRecordedAtDesc(userId);
        }

        return locations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 기간의 위치 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<LocationHistoryResponse> getLocationHistoryByDateRange(
            Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("기간별 위치 히스토리 조회: userId={}, start={}, end={}", userId, startDate, endDate);

        List<LocationHistory> locations = locationHistoryRepository
                .findByUserIdAndRecordedAtBetween(userId, startDate, endDate);

        return locations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 최근 24시간 이내 위치 조회
     */
    @Transactional(readOnly = true)
    public List<LocationHistoryResponse> getRecentLocations(Long userId) {
        logger.info("최근 24시간 위치 조회: userId={}", userId);

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<LocationHistory> locations = locationHistoryRepository
                .findRecentLocationsByUserId(userId, since);

        return locations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 역지오코딩: 좌표를 주소로 변환
     * (실제 구현 시 Kakao, Google Maps, Naver 등의 API 사용)
     */
    public String reverseGeocode(Double latitude, Double longitude) {
        try {
            // TODO: 실제 지오코딩 API 연동
            // 예시: Kakao Local API, Google Geocoding API, Naver Maps API

            // 임시 구현 (실제로는 외부 API 호출)
            logger.info("역지오코딩 요청: lat={}, lng={}", latitude, longitude);

            // Kakao Local API 사용 예시 (실제 구현 시 주석 해제)
            /*
            String kakaoApiKey = "YOUR_KAKAO_API_KEY";
            String url = String.format(
                "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=%f&y=%f",
                longitude, latitude
            );

            WebClient webClient = webClientBuilder.build();
            String response = webClient.get()
                .uri(url)
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // JSON 파싱 후 주소 추출
            */

            // 임시 반환값
            return String.format("위도: %.6f, 경도: %.6f 부근", latitude, longitude);

        } catch (Exception e) {
            logger.error("역지오코딩 실패: {}", e.getMessage());
            return String.format("좌표: %.6f, %.6f", latitude, longitude);
        }
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private LocationHistoryResponse convertToResponse(LocationHistory location) {
        return LocationHistoryResponse.builder()
                .id(location.getId())
                .userId(location.getUser().getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .accuracy(location.getAccuracy())
                .recordedAt(location.getRecordedAt())
                .build();
    }
}