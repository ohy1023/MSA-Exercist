package com.example.userservice.user.controller;

import com.example.userservice.user.domain.MessageResponse;
import com.example.userservice.user.domain.Response;
import com.example.userservice.user.domain.dto.LoginResponse;
import com.example.userservice.user.domain.dto.LoginTempResponse;
import com.example.userservice.user.domain.dto.UserJoinRequest;
import com.example.userservice.user.domain.dto.UserLoginRequest;
import com.example.userservice.user.service.UserService;
import com.example.userservice.user.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final Environment env;

    @GetMapping("/test")
    public ResponseEntity<?> test(Authentication authentication) {
        log.info("email : {}",authentication.getName());
        return ResponseEntity.ok("test 성공");
    }


    @PostMapping("/refresh")
    public Response<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {

        Optional<String> optionalRefreshToken = CookieUtils.extractRefreshToken(request);

        LoginTempResponse loginTempResponse = userService.reissue(optionalRefreshToken.get());

        String accessToken = loginTempResponse.getAccessToken();

        String refreshToken = loginTempResponse.getRefreshToken();

        CookieUtils.addRefreshTokenAtCookie(response, refreshToken);

        return Response.success(new LoginResponse(accessToken));
    }

    @PostMapping("/join")
    public Response<MessageResponse> join(@RequestBody UserJoinRequest reqeust) {
        MessageResponse response = userService.join(reqeust);
        return Response.success(response);
    }

    @PostMapping("/login")
    public Response<LoginResponse> login(@RequestBody UserLoginRequest customerLoginRequest, HttpServletRequest request,
                                         HttpServletResponse response) {

        LoginTempResponse loginTempResponse = userService.login(customerLoginRequest);

        String accessToken = loginTempResponse.getAccessToken();

        String refreshToken = loginTempResponse.getRefreshToken();

        CookieUtils.addRefreshTokenAtCookie(response, refreshToken);

        return Response.success(new LoginResponse(accessToken));
    }

    @GetMapping("/health_check")
    public String status(Authentication authentication) {
        log.info("email : {}",authentication.getName());
        return String.format("It's Working in User Service"
                + ", port(local.server.port) = " + env.getProperty("local.server.port")
                + ", port(server.port) = " + env.getProperty("server.port")
                + ", token secret = " + env.getProperty("jwt.secret")
                + ", token expiration time = " + env.getProperty("jwt.access.expiration")
                + ", token refresh time = " + env.getProperty("jwt.refresh.expiration"));
    }
}
