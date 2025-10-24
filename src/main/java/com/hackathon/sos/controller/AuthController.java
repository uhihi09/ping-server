package com.hackathon.sos.controller;

import com.hackathon.sos.dto.request.LoginRequest;
import com.hackathon.sos.dto.request.SignupRequest;
import com.hackathon.sos.dto.response.ApiResponse;
import com.hackathon.sos.dto.response.JwtResponse;
import com.hackathon.sos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<JwtResponse>> signup(@Valid @RequestBody SignupRequest request) {
        logger.info("회원가입 API 호출: username={}", request.getUsername());

        JwtResponse response = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다", response));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("로그인 API 호출: usernameOrEmail={}", request.getUsernameOrEmail());

        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다", response));
    }

    /**
     * 로그아웃 (클라이언트에서 토큰 삭제)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        logger.info("로그아웃 API 호출");

        // JWT 방식에서는 클라이언트에서 토큰을 삭제하면 됨
        // 필요시 토큰 블랙리스트 구현 가능
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다", null));
    }
}