package com.agrimarket.repo;

import com.agrimarket.model.CartItem;
import com.agrimarket.model.Product;
import com.agrimarket.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    List<CartItem> findByProduct(Product product);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
}
