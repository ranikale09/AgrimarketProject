package com.agrimarket.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Long productId;
    public Long farmerId;
    public String productName;
    public String emoji;
    public String unit;
    public BigDecimal price;
    public int quantity;
}
