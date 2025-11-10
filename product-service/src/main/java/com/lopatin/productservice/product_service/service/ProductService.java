package com.lopatin.productservice.product_service.service;

import com.lopatin.productservice.product_service.dto.ProductRequest;
import com.lopatin.productservice.product_service.dto.ProductResponse;
import com.lopatin.productservice.product_service.model.Product;
import com.lopatin.productservice.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Service
public class ProductService {

    private final ProductRepository productRepository;



    public void createProduct(ProductRequest productRequest){
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product with id = {} successfully saved", product.getId());
    }




    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }



    // MAPPER temporary
    private ProductResponse mapToResponse(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}

