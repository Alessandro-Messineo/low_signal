package org.ecommerce.java.low_signal.service;

import java.util.List;
import java.util.Optional;

import org.ecommerce.java.low_signal.model.Product;
import org.ecommerce.java.low_signal.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public Optional<Product> getProductBySlug(String slug){
        return productRepository.findBySlug(slug);
    }

}
