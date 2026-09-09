package com.example.demo_test.controller;


import com.example.demo_test.exception.ProductNotFoundException;
import com.example.demo_test.model.Product;
import com.example.demo_test.service.ProductService;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnList() throws Exception {
        Product p1 = new Product(1L, "Laptop", 55000.0, 10);
        Product p2 = new Product(2L, "Mouse", 799.0, 50);
        given(productService.getAllProducts()).willReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() throws Exception {
        Product product = new Product(1L, "Laptop", 55000.0, 10);
        given(productService.getProductById(1L)).willReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(55000.0));
    }

    @Test
    void getProductById_shouldReturn404_whenNotFound() throws Exception {
        given(productService.getProductById(999L))
                .willThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void createProduct_shouldReturn201_whenValid() throws Exception {
        Product input = new Product(null, "Keyboard", 1500.0, 20);
        Product saved = new Product(3L, "Keyboard", 1500.0, 20);
        given(productService.createProduct(any(Product.class))).willReturn(saved);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void createProduct_shouldReturn400_whenNameMissing() throws Exception {
        Product invalid = new Product(null, "", 100.0, 1); // blank name fails @NotBlank

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenExists() throws Exception {
        Product update = new Product(null, "Updated Name", 999.0, 5);
        Product updated = new Product(1L, "Updated Name", 999.0, 5);
        given(productService.updateProduct(anyLong(), any(Product.class))).willReturn(updated);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateProduct_shouldReturn404_whenNotFound() throws Exception {
        Product update = new Product(null, "Doesn't matter", 1.0, 1);
        doThrow(new ProductNotFoundException(999L))
                .when(productService).updateProduct(anyLong(), any(Product.class));

        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_shouldReturn204_whenExists() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ProductNotFoundException(999L)).when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
