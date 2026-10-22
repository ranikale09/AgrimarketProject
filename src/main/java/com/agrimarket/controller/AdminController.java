package com.agrimarket.controller;

import com.agrimarket.model.Order;
import com.agrimarket.model.User;
import com.agrimarket.repo.OrderRepository;
import com.agrimarket.repo.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final OrderRepository orders;

    public AdminController(UserRepository users, OrderRepository orders) { this.users = users; this.orders = orders; }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        BigDecimal revenue = orders.findAll().stream().map((Order o) -> o.total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("users", users.count(), "farmers", users.countByRole(User.Role.FARMER),
                "orders", orders.count(), "revenue", revenue);
    }

    @GetMapping("/users")
    public List<User> users() { return users.findAll(); }
}
