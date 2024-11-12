package com.example.catagoryservice.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public Iterable<Catalog> getAllCatalogs() {
        return catalogRepository.findAll();
    }


}
