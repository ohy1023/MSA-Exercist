package com.example.userservice.user.controller;

import com.example.userservice.user.domain.MessageResponse;
import com.example.userservice.user.domain.Response;
import com.example.userservice.user.domain.dto.LoginResponse;
import com.example.userservice.user.domain.dto.UserJoinRequest;
import com.example.userservice.user.domain.dto.UserLoginRequest;
import com.example.userservice.user.service.UserService;
import com.example.userservice.user.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("test 성공");
    }


    @PostMapping("/join")
    public Response<MessageResponse> join(@RequestBody UserJoinRequest reqeust) {
        MessageResponse response = userService.join(reqeust);
        return Response.success(response);
    }

    @PostMapping("/login")
    public Response<LoginResponse> login(@RequestBody UserLoginRequest customerLoginRequest, HttpServletRequest request,
                                         HttpServletResponse response) {

        LoginResponse loginResponse = userService.login(customerLoginRequest);

        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();


        CookieUtils.addAccessTokenAtCookie(response, accessToken);

        CookieUtils.addRefreshTokenAtCookie(response, refreshToken);

        return Response.success(loginResponse);
    }
}
