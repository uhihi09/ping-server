package com.hackathon.sos.service;

import com.hackathon.sos.dto.request.LoginRequest;
import com.hackathon.sos.dto.request.SignupRequest;
import com.hackathon.sos.dto.response.JwtResponse;
import com.hackathon.sos.entity.User;
import com.hackathon.sos.repository.UserRepository;
import com.hackathon.sos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public JwtResponse signup(SignupRequest request) {
        logger.info("회원가입 요청: username={}", request.getUsername());

        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        if (request.getDeviceId() != null && userRepository.existsByDeviceId(request.getDeviceId())) {
            throw new IllegalArgumentException("이미 등록된 장치 ID입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .deviceId(request.getDeviceId())
                .role(User.UserRole.USER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("회원가입 완료: userId={}", savedUser.getId());

        // 자동 로그인
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .phoneNumber(savedUser.getPhoneNumber())
                .deviceId(savedUser.getDeviceId())
                .build();
    }

    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        logger.info("로그인 요청: usernameOrEmail={}", request.getUsernameOrEmail());

        // 사용자명 또는 이메일로 찾기
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        logger.info("로그인 성공: userId={}", user.getId());

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .deviceId(user.getDeviceId())
                .build();
    }
}