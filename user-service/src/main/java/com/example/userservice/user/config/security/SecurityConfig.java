package com.example.userservice.user.config.security;

import com.example.userservice.user.config.token.JwtExceptionFilter;
import com.example.userservice.user.config.token.JwtFilter;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.user.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .cors(AbstractHttpConfigurer::disable) // CORS 설정 (필요에 따라 설정)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users/login", "/users/join").permitAll() // 로그인, 회원가입 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .addFilterBefore(new JwtFilter(userRepository, jwtUtils), UsernamePasswordAuthenticationFilter.class) // JWT 필터 추가
                .addFilterBefore(new JwtExceptionFilter(), JwtFilter.class); // JWT 예외 필터 추가

        return http.build();
    }

}
