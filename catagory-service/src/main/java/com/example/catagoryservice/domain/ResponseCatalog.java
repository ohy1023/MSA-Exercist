package com.example.catagoryservice.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ResponseCatalog {
    private String productId;
    private String productName;
    private Integer unitPrice;
    private Integer stock;
    private LocalDateTime createdAt;
}
