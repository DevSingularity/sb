package org.example.createrstore.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.createrstore.entities.Product;
import org.example.createrstore.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productservice;

    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productservice.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return productservice.updateProduct(id, product);
    }

    @GetMapping
    public List<Product> getProducts() {
        return productservice.getProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productservice.getProductById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productservice.deleteProduct(id);
        return;
    }
}
