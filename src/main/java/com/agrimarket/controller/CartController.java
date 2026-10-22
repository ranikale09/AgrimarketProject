package com.agrimarket.controller;

import static com.agrimarket.controller.CurrentUser.bad;

import com.agrimarket.model.CartItem;
import com.agrimarket.model.Product;
import com.agrimarket.model.User;
import com.agrimarket.repo.CartItemRepository;
import com.agrimarket.repo.ProductRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    record AddReq(Long productId, int quantity) {}
    record QtyReq(int quantity) {}

    private final CartItemRepository cart;
    private final ProductRepository products;
    private final CurrentUser current;

    public CartController(CartItemRepository cart, ProductRepository products, CurrentUser current) {
        this.cart = cart; this.products = products; this.current = current;
    }

    @GetMapping
    public List<CartItem> list(Authentication auth) { return cart.findByUser(current.get(auth)); }

    @PostMapping
    public CartItem add(Authentication auth, @RequestBody AddReq r) {
        User me = current.get(auth);
        Product p = products.findById(r.productId()).orElseThrow(() -> bad("Product not found"));
        int qty = Math.max(1, r.quantity());
        CartItem item = cart.findByUserAndProduct(me, p).orElseGet(() -> {
            CartItem c = new CartItem(); c.user = me; c.product = p; c.quantity = 0; return c;
        });
        if (item.quantity + qty > p.stock) throw bad("Only " + p.stock + " " + p.unit + " of " + p.name + " available");
        item.quantity += qty;
        return cart.save(item);
    }

    @PutMapping("/{id}")
    public CartItem update(Authentication auth, @PathVariable Long id, @RequestBody QtyReq r) {
        CartItem item = owned(auth, id);
        if (r.quantity() < 1) throw bad("Quantity must be at least 1");
        if (r.quantity() > item.product.stock) throw bad("Only " + item.product.stock + " available");
        item.quantity = r.quantity();
        return cart.save(item);
    }

    @DeleteMapping("/{id}")
    public void remove(Authentication auth, @PathVariable Long id) { cart.delete(owned(auth, id)); }

    private CartItem owned(Authentication auth, Long id) {
        User me = current.get(auth);
        CartItem item = cart.findById(id).orElseThrow(() -> bad("Cart item not found"));
        if (!item.user.id.equals(me.id)) throw bad("Cart item not found");
        return item;
    }
}
