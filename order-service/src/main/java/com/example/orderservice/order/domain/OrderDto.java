package com.example.orderservice.order.domain;

import lombok.Data;

@Data
public class OrderDto {

    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private String orderId;
    private Long userId;

}
