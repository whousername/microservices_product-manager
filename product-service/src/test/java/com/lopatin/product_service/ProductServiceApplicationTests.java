package com.lopatin.product_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lopatin.product_service.config.TestSecurityConfig;
import com.lopatin.product_service.dto.ProductRequest;
import com.lopatin.product_service.model.Product;
import com.lopatin.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.mongodb.uri",mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

	@Test
	void createProduct_shouldCreateProduct() throws Exception {
        ProductRequest productRequest = getProductRequest();

        mockMvc.perform(post("/api/product")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated());
        Assertions.assertEquals(1, productRepository.findAll().size());
	}


    @Test
    void getAllProducts_shouldReturnListProductResponse() throws Exception {
        var product1 = Product.builder().name("iPhone15").description("New one").price(BigDecimal.valueOf(1000)).build();
        var product2 = Product.builder().name("iPhone16").description("New one").price(BigDecimal.valueOf(1200)).build();
        productRepository.saveAll(List.of(product1, product2));

        mockMvc.perform(get("/api/product"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("iPhone15"))
                .andExpect(jsonPath("$[1].name").value("iPhone16"));

    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("Iphone17")
                .description("New one")
                .price(BigDecimal.valueOf(1500))
                .build();
    }

}
