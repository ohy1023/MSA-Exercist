package com.example.userservice.user.config.redis.service;


import com.example.userservice.user.config.redis.entity.TokenEntity;
import com.example.userservice.user.config.redis.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    @Transactional(readOnly = true)
    public TokenEntity findByUserId(String userId) {
        return tokenRepository.findByUserId(userId)
                              .orElseThrow(() -> new RuntimeException());
    }

    @Transactional(readOnly = true)
    public TokenEntity findByAccessToken(String accessToken) {
        return tokenRepository.findByAccessToken(accessToken)
                              .orElseThrow(() -> new RuntimeException());
    }

    @Transactional(readOnly = true)
    public TokenEntity findByRefreshToken(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken)
                              .orElseThrow(() -> new RuntimeException());
    }

    @Transactional
    public void saveTokens(Long userId, String refreshToken, String accessToken) {

        // Stream 형태 Ex {userId:1, {refreshToken:xxx, accessToken:xxx}}
        log.info("[Redis] Tokens Saved!");
        log.info("[userId: {}, [refreshToken: {}, accessToken: {}]]", userId, refreshToken, accessToken);
        tokenRepository.save(new TokenEntity(String.valueOf(userId), refreshToken, accessToken));
    }

    @Transactional
    public void removeTokenByUserId(String userId) {

        log.info("[Redis] Token Removed! / userId: {}", userId);
        tokenRepository.findByUserId(userId)
                       .ifPresent(tokenRepository::delete);
    }

    @Transactional
    public void removeToken(String accessToken) {

        log.info("[Redis] Token Removed! / accessToken: {}", accessToken);
        tokenRepository.findByAccessToken(accessToken)
                       .ifPresent(tokenRepository::delete);
    }

    @Transactional
    public void removeTokenByRefreshToken(String refreshToken) {

        log.info("[Redis] Token Removed! / refreshToken: {}", refreshToken);
        tokenRepository.findByRefreshToken(refreshToken)
                       .ifPresent(tokenRepository::delete);
    }

    @Transactional
    public void removeTokens(String accessToken, String refreshToken) {

        log.info("[Redis] Tokens Removed! / accessToken: {}, refreshToken: {}", accessToken, refreshToken);
        tokenRepository.findByAccessTokenAndRefreshToken(accessToken, refreshToken)
                       .ifPresent(tokenRepository::delete);
    }
}
