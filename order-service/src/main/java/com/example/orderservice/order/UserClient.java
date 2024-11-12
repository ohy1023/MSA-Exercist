package com.example.orderservice.order;

import com.example.orderservice.order.domain.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://user-service") // Eureka 등으로 URL을 설정
public interface UserClient {
    @GetMapping("/users/{userId}")
    User getUserById(@PathVariable("userId") Long userId);
}