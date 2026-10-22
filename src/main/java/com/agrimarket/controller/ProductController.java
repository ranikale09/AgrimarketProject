package com.agrimarket.controller;

import com.agrimarket.model.Product;
import com.agrimarket.repo.ProductRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String sort,
                              @RequestParam(required = false) Boolean inStock) {
        var s = repo.findAll().stream();
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all"))
            s = s.filter(p -> category.equalsIgnoreCase(p.category));
        if (q != null && !q.isBlank()) {
            String t = q.toLowerCase();
            s = s.filter(p -> (p.name + " " + (p.farmer != null ? p.farmer.name : "")).toLowerCase().contains(t));
        }
        if (Boolean.TRUE.equals(inStock)) s = s.filter(p -> p.stock > 0);
        Comparator<Product> order = switch (sort == null ? "" : sort) {
            case "price_asc" -> Comparator.comparing((Product p) -> p.price);
            case "price_desc" -> Comparator.comparing((Product p) -> p.price).reversed();
            case "newest" -> Comparator.comparing((Product p) -> p.id).reversed();
            default -> Comparator.comparingInt((Product p) -> p.reviews).reversed();
        };
        return s.sorted(order).toList();
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
