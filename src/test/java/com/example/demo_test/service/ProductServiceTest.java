package com.example.demo_test.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo_test.exception.ProductNotFoundException;
import com.example.demo_test.model.Product;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Fresh instance for each test (constructor seeds 2 sample products)
        productService = new ProductService();
    }

    @Test
    void shouldReturnSeededProducts_onStartup() {
        Collection<Product> products = productService.getAllProducts();

        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Wireless Mouse");
    }

    @Test
    void shouldCreateProduct_andAssignId() {
        Product newProduct = new Product(null, "Keyboard", 1500.0, 20);

        Product created = productService.createProduct(newProduct);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Keyboard");
        assertThat(productService.getAllProducts()).hasSize(3);
    }

    @Test
    void shouldReturnProduct_whenIdExists() {
        Product created = productService.createProduct(new Product(null, "Monitor", 12000.0, 5));

        Product found = productService.getProductById(created.getId());

        assertThat(found.getName()).isEqualTo("Monitor");
        assertThat(found.getPrice()).isEqualTo(12000.0);
    }

    @Test
    void shouldThrowException_whenProductIdDoesNotExist() {
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldUpdateProduct_whenIdExists() {
        Product created = productService.createProduct(new Product(null, "Old Name", 100.0, 1));

        Product updated = productService.updateProduct(
                created.getId(), new Product(null, "New Name", 200.0, 2));

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getPrice()).isEqualTo(200.0);
        assertThat(updated.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistentProduct() {
        Product update = new Product(null, "Doesn't matter", 1.0, 1);

        assertThatThrownBy(() -> productService.updateProduct(999L, update))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldDeleteProduct_whenIdExists() {
        Product created = productService.createProduct(new Product(null, "ToDelete", 50.0, 1));
        int sizeBefore = productService.getAllProducts().size();

        productService.deleteProduct(created.getId());

        assertThat(productService.getAllProducts()).hasSize(sizeBefore - 1);
        assertThat(productService.existsById(created.getId())).isFalse();
    }

    @Test
    void shouldThrowException_whenDeletingNonExistentProduct() {
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldConfirmExistsById_forValidAndInvalidIds() {
        Product created = productService.createProduct(new Product(null, "Exists", 10.0, 1));

        assertThat(productService.existsById(created.getId())).isTrue();
        assertThat(productService.existsById(9999L)).isFalse();
    }
}
