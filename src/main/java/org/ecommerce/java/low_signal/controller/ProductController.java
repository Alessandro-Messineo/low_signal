package org.ecommerce.java.low_signal.controller;

import java.util.List;
import java.util.Optional;

import org.ecommerce.java.low_signal.dto.ProductDTO;
import org.ecommerce.java.low_signal.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {

        Optional<ProductDTO> product = productService.getProductBySlug(slug);

        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{slug}/related")
    public ResponseEntity<List<ProductDTO>> getRelatedProducts(@PathVariable String slug) {
        List<ProductDTO> related = productService.getRelatedProducts(slug);
        return ResponseEntity.ok(related);
    }
}