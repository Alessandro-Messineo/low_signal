package org.ecommerce.java.low_signal.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ecommerce.java.low_signal.dto.ProductDTO;
import org.ecommerce.java.low_signal.model.Collection;
import org.ecommerce.java.low_signal.model.Product;
import org.ecommerce.java.low_signal.repository.CollectionRepository;
import org.ecommerce.java.low_signal.repository.ProductRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CollectionRepository collectionRepository;

    public ProductService(ProductRepository productRepository, CollectionRepository collectionRepository) {
        this.productRepository = productRepository;
        this.collectionRepository = collectionRepository;
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

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = toEntity(dto, new Product());
        Product saved = productRepository.save(product);
        return ProductDTO.fromEntity(saved);
    }

    @Transactional
    public Optional<ProductDTO> updateProduct(Integer id, ProductDTO dto) {
        return productRepository.findById(id)
                .map(existing -> {
                    Product updated = toEntity(dto, existing);
                    return ProductDTO.fromEntity(productRepository.save(updated));
                });
    }

    private Product toEntity(ProductDTO dto, Product product) {
        Collection collection = collectionRepository.findById(dto.getCollectionId())
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        product.setName(dto.getName());
        product.setSlug(dto.getSlug());
        product.setTag(dto.getTag());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setComparePrice(dto.getComparePrice());
        product.setFeatured(dto.getFeatured());
        product.setCollection(collection);
        return product;
    }

}
