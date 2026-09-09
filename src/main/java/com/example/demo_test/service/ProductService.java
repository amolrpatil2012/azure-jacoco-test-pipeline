package com.example.demo_test.service;


import org.springframework.stereotype.Service;

import com.example.demo_test.exception.ProductNotFoundException;
import com.example.demo_test.model.Product;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory Product service. No database is used -
 * data is stored in a thread-safe Map and resets on app restart.
 */
@Service
public class ProductService {

    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public ProductService() {
        // Seed with a couple of sample products on startup
        createProduct(new Product(null, "Laptop", 55000.0, 10));
        createProduct(new Product(null, "Wireless Mouse", 799.0, 50));
    }

    public Collection<Product> getAllProducts() {
        return productStore.values();
    }

    public Product getProductById(Long id) {
        Product product = productStore.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public Product createProduct(Product product) {
        long newId = idCounter.incrementAndGet();
        product.setId(newId);
        productStore.put(newId, product);
        return product;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = getProductById(id); // throws if not found
        existing.setName(updatedProduct.getName());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        productStore.put(id, existing);
        return existing;
    }

    public void deleteProduct(Long id) {
        if (!productStore.containsKey(id)) {
            throw new ProductNotFoundException(id);
        }
        productStore.remove(id);
    }

    public boolean existsById(Long id) {
        return productStore.containsKey(id);
    }
}
