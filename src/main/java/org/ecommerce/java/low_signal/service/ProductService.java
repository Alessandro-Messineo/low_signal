package org.ecommerce.java.low_signal.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ecommerce.java.low_signal.dto.ProductDTO;
import org.ecommerce.java.low_signal.model.Product;
import org.ecommerce.java.low_signal.repository.ProductRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<ProductDTO> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(ProductDTO::fromEntity);
    }

    @Transactional
    public List<ProductDTO> getRelatedProducts(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return productRepository
                .findByCollectionIdAndSlugNot(product.getCollection().getId(), slug)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
