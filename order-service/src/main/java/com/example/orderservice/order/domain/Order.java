package com.example.orderservice.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String productId;

    private String orderId;

    private Integer qty;

    private Integer unitPrice;

    private Integer totalPrice;

    private LocalDateTime orderDate;




}