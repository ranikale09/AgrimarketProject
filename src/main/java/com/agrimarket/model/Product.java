package com.agrimarket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    @Column(length = 1000) public String description;
    public BigDecimal price;
    public String unit = "kg";
    public String category;
    public int stock;
    public String emoji;
    public boolean organic = true;
    public double rating = 4.5;
    public int reviews;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "farmer_id")
    @JsonIgnoreProperties({"email", "phone"})
    public User farmer;
}
