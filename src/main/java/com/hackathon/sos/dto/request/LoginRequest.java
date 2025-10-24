package com.hackathon.sos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "사용자명 또는 이메일은 필수입니다")
    private String usernameOrEmail;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}