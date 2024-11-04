package com.example.userservice.user.service;

import com.example.userservice.user.config.redis.RedisDao;
import com.example.userservice.user.domain.MessageResponse;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.dto.LoginResponse;
import com.example.userservice.user.domain.dto.TokenRequest;
import com.example.userservice.user.domain.dto.UserJoinRequest;
import com.example.userservice.user.domain.dto.UserLoginRequest;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.user.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;



@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder encoder;
    private final RedisDao redisDao;
    private final JwtUtils jwtUtils;

    @Value("${jwt.secret}")
    public String secretKey;
    @Value("${jwt.access.expiration}")
    public Long accessTokenExpiration;
    @Value("${jwt.refresh.expiration}")
    public Long refreshTokenExpiration;
    @Value("${access-token-maxage}")
    public int accessTokenMaxAge;
    @Value("${refresh-token-maxage}")
    public int refreshTokenMaxAge;

    @Transactional
    public MessageResponse join(UserJoinRequest request) {
        log.info("회원가입 요청 : {}", request);

        userRepository.findByEmail(request.getEmail())
                .ifPresent(customer -> {
                    throw new RuntimeException();
                });


        User user = request.toEntity(encoder.encode(request.getPassword()));
        userRepository.save(user);

        return new MessageResponse("회원가입 성공");
    }

    @Transactional
    public LoginResponse login(UserLoginRequest request) {

        User findUser = findUserByEmail(request.getEmail());

        if (mismatchPassword(request.getPassword(), findUser.getPassword())) {
            throw new RuntimeException();
        }

        String accessToken = jwtUtils.createAccessToken(request.getEmail());
        String refreshToken = jwtUtils.createRefreshToken(request.getEmail());

        if (accessToken == null || refreshToken == null) {
            throw new RuntimeException();
        }

        // 저장 형태 {"RT:test@test.com" , "refreshToken"}
        redisDao.setValues("RT:" + findUser.getEmail(), refreshToken, refreshTokenMaxAge, TimeUnit.SECONDS);

        return new LoginResponse(accessToken, refreshToken);
    }

//    @Transactional
//    public LoginResponse reissue(TokenRequest request, String email) {
//
//        Customer findCustomer = findCustomerByEmail(email);
//
//        if (jwtUtils.isExpired(request.getRefreshToken())) {
//            throw new AppException(INVALID_TOKEN, INVALID_TOKEN.getMessage());
//        }
//
//        String refreshToken = redisDao.getValues("RT:" + email);
//
//        if (ObjectUtils.isEmpty(refreshToken)) {
//            throw new AppException(INVALID_REQUEST, INVALID_REQUEST.getMessage());
//        }
//        if (!refreshToken.equals(request.getRefreshToken())) {
//            throw new AppException(INVALID_TOKEN, INVALID_TOKEN.getMessage());
//        }
//
//        String newAccessToken = jwtUtils.createAccessToken(findCustomer.getEmail());
//        String newRefreshToken = jwtUtils.createRefreshToken(findCustomer.getEmail());
//
//        // 저장 형태 {"RT:test@test.com" , "refreshToken"}
//        redisDao.setValues("RT:" + findCustomer.getEmail(), newRefreshToken, refreshTokenMaxAge, TimeUnit.SECONDS);
//
//        return new LoginResponse(newAccessToken, newRefreshToken);
//
//    }

    @Transactional
    public MessageResponse logout(TokenRequest request, String email) {

        User findUser = findUserByEmail(email);

        String accessToken = request.getAccessToken();

        if (jwtUtils.isExpired(accessToken)) {
            throw new RuntimeException();
        }

        if (jwtUtils.isValid(accessToken)) {
            throw new RuntimeException();
        }

        // Token 삭제
        redisDao.deleteValues("RT:" + findUser.getEmail());

        int expiration = jwtUtils.getExpiration(request.getAccessToken()).intValue() / 1000;

        log.info("expiration = {}sec", expiration);

        redisDao.setValues(request.getAccessToken(), "logout", expiration, TimeUnit.SECONDS);

        return new MessageResponse("로그아웃 되었습니다.");
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException());
    }

    private boolean mismatchPassword(String rawPassword, String encodedPassword) {
        return !encoder.matches(rawPassword, encodedPassword);
    }
}
