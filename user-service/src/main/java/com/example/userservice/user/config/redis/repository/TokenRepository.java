package com.example.userservice.user.config.redis.repository;

import com.example.userservice.user.config.redis.entity.TokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends CrudRepository<TokenEntity, String> {

    Optional<TokenEntity> findByAccessToken(String accessToken);
    Optional<TokenEntity> findByRefreshToken(String refreshToken);
    Optional<TokenEntity> findByUserId(String userId);
    Optional<TokenEntity> findByAccessTokenAndRefreshToken(String accessToken, String refreshToken);

}
