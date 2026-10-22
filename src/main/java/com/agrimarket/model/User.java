package com.agrimarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    public enum Role { CUSTOMER, FARMER, ADMIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    @Column(unique = true, nullable = false) public String email;
    @JsonIgnore public String password;
    @Enumerated(EnumType.STRING) public Role role = Role.CUSTOMER;
    public String phone;
    public String location;
    public boolean verified = true;
}
