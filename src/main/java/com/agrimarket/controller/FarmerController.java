package com.agrimarket.controller;

import static com.agrimarket.controller.CurrentUser.bad;

import com.agrimarket.model.*;
import com.agrimarket.repo.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {
    record StatusReq(String status) {}

    private final ProductRepository products;
    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final CurrentUser current;

    public FarmerController(ProductRepository products, OrderRepository orders, CartItemRepository cart, CurrentUser current) {
        this.products = products; this.orders = orders; this.cart = cart; this.current = current;
    }

    @GetMapping("/products")
    public List<Product> myProducts(Authentication auth) { return products.findByFarmerId(current.get(auth).id); }

    @PostMapping("/products")
    public Product create(Authentication auth, @RequestBody Product in) {
        validate(in);
        Product p = new Product();
        copy(in, p);
        p.farmer = current.get(auth);
        return products.save(p);
    }

    @PutMapping("/products/{id}")
    public Product update(Authentication auth, @PathVariable Long id, @RequestBody Product in) {
        validate(in);
        Product p = ownedProduct(auth, id);
        copy(in, p);
        return products.save(p);
    }

    @DeleteMapping("/products/{id}")
    @Transactional
    public void delete(Authentication auth, @PathVariable Long id) {
        Product p = ownedProduct(auth, id);
        cart.deleteAll(cart.findByProduct(p));
        products.delete(p);
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> myOrders(Authentication auth) { return farmerOrders(current.get(auth)); }

    @PutMapping("/orders/{id}/status")
    public Map<String, String> setStatus(Authentication auth, @PathVariable Long id, @RequestBody StatusReq r) {
        User me = current.get(auth);
        if (!OrderController.STATUSES.contains(r.status())) throw bad("Unknown status");
        Order o = orders.findById(id).orElseThrow(() -> bad("Order not found"));
        if (o.items.stream().noneMatch(i -> me.id.equals(i.farmerId))) throw bad("Order not found");
        o.status = r.status();
        orders.save(o);
        return Map.of("status", o.status);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(Authentication auth) {
        User me = current.get(auth);
        List<Map<String, Object>> mine = farmerOrders(me);
        BigDecimal revenue = mine.stream().map(m -> (BigDecimal) m.get("amount")).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("products", products.findByFarmerId(me.id).size(), "orders", mine.size(), "revenue", revenue);
    }

    private List<Map<String, Object>> farmerOrders(User me) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Order o : orders.findAllByOrderByCreatedAtDesc()) {
            List<OrderItem> mine = o.items.stream().filter(i -> me.id.equals(i.farmerId)).toList();
            if (mine.isEmpty()) continue;
            BigDecimal amount = mine.stream().map(i -> i.price.multiply(BigDecimal.valueOf(i.quantity)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.id); m.put("orderNumber", o.orderNumber); m.put("customerName", o.customerName);
            m.put("status", o.status); m.put("createdAt", o.createdAt); m.put("items", mine); m.put("amount", amount);
            out.add(m);
        }
        return out;
    }

    private Product ownedProduct(Authentication auth, Long id) {
        User me = current.get(auth);
        Product p = products.findById(id).orElseThrow(() -> bad("Product not found"));
        if (p.farmer == null || !p.farmer.id.equals(me.id)) throw bad("Product not found");
        return p;
    }

    private void validate(Product p) {
        if (p.name == null || p.name.isBlank()) throw bad("Enter a product name");
        if (p.price == null || p.price.signum() <= 0) throw bad("Enter a price greater than 0");
        if (p.stock < 0) throw bad("Stock cannot be negative");
        if (p.category == null || p.category.isBlank()) throw bad("Choose a category");
    }

    private void copy(Product in, Product p) {
        p.name = in.name.trim(); p.description = in.description; p.price = in.price;
        p.unit = in.unit == null || in.unit.isBlank() ? "kg" : in.unit;
        p.category = in.category; p.stock = in.stock;
        p.emoji = in.emoji == null || in.emoji.isBlank() ? "🥬" : in.emoji;
        p.organic = in.organic;
    }
}
