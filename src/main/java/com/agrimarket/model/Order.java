package com.agrimarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String orderNumber;
    @ManyToOne(fetch = FetchType.EAGER) @JsonIgnore public User user;
    public String customerName;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    public List<OrderItem> items = new ArrayList<>();
    public BigDecimal subtotal;
    public BigDecimal deliveryCharge;
    public BigDecimal total;
    public String status = "PLACED";
    @Column(length = 500) public String address;
    public String paymentMethod = "COD";
    public LocalDateTime createdAt = LocalDateTime.now();
}
