package com.agrimarket.controller;

import static com.agrimarket.controller.CurrentUser.bad;

import com.agrimarket.model.*;
import com.agrimarket.repo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    public static final List<String> STATUSES = List.of("PLACED", "CONFIRMED", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED");
    static final BigDecimal DELIVERY_CHARGE = BigDecimal.valueOf(40);
    static final BigDecimal FREE_DELIVERY_ABOVE = BigDecimal.valueOf(500);

    record PlaceReq(String address, String paymentMethod) {}

    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final ProductRepository products;
    private final CurrentUser current;

    public OrderController(OrderRepository orders, CartItemRepository cart, ProductRepository products, CurrentUser current) {
        this.orders = orders; this.cart = cart; this.products = products; this.current = current;
    }

    @GetMapping
    public List<Order> mine(Authentication auth) { return orders.findByUserOrderByCreatedAtDesc(current.get(auth)); }

    @GetMapping("/{id}")
    public Order one(Authentication auth, @PathVariable Long id) {
        User me = current.get(auth);
        Order o = orders.findById(id).orElseThrow(() -> bad("Order not found"));
        if (!o.user.id.equals(me.id)) throw bad("Order not found");
        return o;
    }

    @PostMapping
    @Transactional
    public Order place(Authentication auth, @RequestBody PlaceReq req) {
        User me = current.get(auth);
        List<CartItem> items = cart.findByUser(me);
        if (items.isEmpty()) throw bad("Your cart is empty");
        if (req.address() == null || req.address().isBlank()) throw bad("Enter a delivery address");

        Order o = new Order();
        o.user = me;
        o.customerName = me.name;
        o.address = req.address();
        o.paymentMethod = req.paymentMethod() == null ? "COD" : req.paymentMethod();
        o.orderNumber = "AGM-" + LocalDate.now().getYear() + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem c : items) {
            Product p = c.product;
            if (p.stock < c.quantity) throw bad(p.name + " has only " + p.stock + " " + p.unit + " left");
            p.stock -= c.quantity;
            products.save(p);
            OrderItem oi = new OrderItem();
            oi.productId = p.id;
            oi.farmerId = p.farmer == null ? null : p.farmer.id;
            oi.productName = p.name;
            oi.emoji = p.emoji;
            oi.unit = p.unit;
            oi.price = p.price;
            oi.quantity = c.quantity;
            o.items.add(oi);
            subtotal = subtotal.add(p.price.multiply(BigDecimal.valueOf(c.quantity)));
        }
        o.subtotal = subtotal;
        o.deliveryCharge = subtotal.compareTo(FREE_DELIVERY_ABOVE) >= 0 ? BigDecimal.ZERO : DELIVERY_CHARGE;
        o.total = subtotal.add(o.deliveryCharge);
        orders.save(o);
        cart.deleteAll(items);
        return o;
    }
}
