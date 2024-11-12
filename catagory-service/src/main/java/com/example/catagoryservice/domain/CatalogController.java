package com.example.catagoryservice.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<List<ResponseCatalog>> getCatalogs() {
        Iterable<Catalog> allCatalogs = catalogService.getAllCatalogs();

        // Catalog -> ResponseCatalog로 변환
        List<ResponseCatalog> result = new ArrayList<>();
        for (Catalog catalog : allCatalogs) {
            ResponseCatalog response = new ResponseCatalog();
            response.setProductId(catalog.getProductId());
            response.setProductName(catalog.getProductName());
            response.setUnitPrice(catalog.getUnitPrice());
            response.setStock(catalog.getStock());
            response.setCreatedAt(catalog.getCreatedAt());

            result.add(response);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
