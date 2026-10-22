package com.agrimarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JsonIgnore public User user;
    @ManyToOne(fetch = FetchType.EAGER) public Product product;
    public int quantity;
}
