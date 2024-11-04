package com.example.userservice.user.domain.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {
    private String accessToken;
    private String refreshToken;
}
