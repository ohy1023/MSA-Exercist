package com.example.catagoryservice.domain;

import lombok.Data;

@Data
public class CatalogDto {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private String orderId;
    private Long userId;
}