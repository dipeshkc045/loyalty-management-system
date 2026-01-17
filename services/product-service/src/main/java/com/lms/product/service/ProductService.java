package com.lms.product.service;

import com.lms.product.model.Product;
import com.lms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        if (product.getCode() == null || product.getCode().trim().isEmpty()) {
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required");
            }
            String generatedCode = product.getName().trim().toUpperCase().replace(" ", "_");
            product.setCode(generatedCode);
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Objects.requireNonNull(id, "Product id cannot be null");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        product.setDescription(productDetails.getDescription());
        product.setIsActive(productDetails.getIsActive());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Objects.requireNonNull(id, "Product id cannot be null");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductByCode(String code) {
        return productRepository.findByCode(code);
    }
}
