package com.example.orderservice.order.domain;

import lombok.Data;

@Data
public class RequestOrder {
    private String productId;
    private int qty;
    private Integer unitPrice;
}
