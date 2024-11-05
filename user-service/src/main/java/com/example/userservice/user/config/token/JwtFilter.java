package com.example.userservice.user.config.token;

import com.example.userservice.user.domain.User;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.user.util.CookieUtils;
import com.example.userservice.user.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;
    /**
     * Access Token 은 Header 에 담아서 보내고, Refresh Token 은 Cookie 에 담아서 보낸다.
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().equals("/users/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/users/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().equals("/users/join")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).replace("Bearer ", "");

        String info = jwtUtils.getEmail(accessToken);

        User user = userRepository.findByEmail(info)
                .orElseThrow(() -> new RuntimeException());

        // 유효성 검증 통과한 경우
        log.info("SecurityContextHolder 에 Authentication 객체를 저장합니다!");
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

}
