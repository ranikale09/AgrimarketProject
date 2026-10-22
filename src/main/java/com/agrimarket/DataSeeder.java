package com.agrimarket;

import com.agrimarket.model.Product;
import com.agrimarket.model.User;
import com.agrimarket.repo.ProductRepository;
import com.agrimarket.repo.UserRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates demo accounts and products the first time the app starts on an empty database. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, ProductRepository products, PasswordEncoder encoder) {
        this.users = users; this.products = products; this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        user("Admin", "admin@agrimarket.com", "admin123", User.Role.ADMIN, "Pune");
        user("Rani Sharma", "rani@example.com", "password123", User.Role.CUSTOMER, "Nashik");
        User ramesh = user("Ramesh Kumar", "ramesh@agrimarket.com", "password123", User.Role.FARMER, "Nashik, Maharashtra");
        User sita = user("Sita Devi", "sita@agrimarket.com", "password123", User.Role.FARMER, "Pune, Maharashtra");
        User mahesh = user("Mahesh Patel", "mahesh@agrimarket.com", "password123", User.Role.FARMER, "Satara, Maharashtra");

        product(ramesh, "Tomatoes", "Vegetables", 25, "kg", 120, "🍅", 4.5, 120, "Fresh and juicy tomatoes grown with natural farming methods. Perfect for salads, sauces and daily cooking.");
        product(ramesh, "Onions", "Vegetables", 22, "kg", 200, "🧅", 4.3, 98, "Firm red onions with a sharp flavour, harvested this week.");
        product(sita, "Potatoes", "Vegetables", 20, "kg", 250, "🥔", 4.4, 110, "Organic potatoes, good for frying, boiling and curries.");
        product(sita, "Carrots", "Vegetables", 18, "kg", 90, "🥕", 4.6, 87, "Sweet, crunchy carrots pulled fresh from the field.");
        product(mahesh, "Capsicum", "Vegetables", 30, "kg", 60, "🫑", 4.3, 54, "Glossy green capsicum, great for stir-fries.");
        product(mahesh, "Brinjal", "Vegetables", 25, "kg", 70, "🍆", 4.1, 41, "Tender purple brinjal for bharta and curries.");
        product(ramesh, "Cabbage", "Vegetables", 22, "kg", 80, "🥬", 4.2, 39, "Compact green cabbage heads.");
        product(sita, "Spinach", "Vegetables", 15, "kg", 50, "🥬", 4.6, 76, "Leafy spinach, washed and bunched on the morning of dispatch.");
        product(mahesh, "Basmati Rice", "Grains", 60, "kg", 300, "🍚", 4.8, 145, "Long-grain aged basmati from local mills.");
        product(sita, "Jowar", "Grains", 45, "kg", 150, "🌾", 4.4, 33, "Whole jowar, stone-ground on request.");
        product(ramesh, "Bananas", "Fruits", 40, "dozen", 100, "🍌", 4.5, 66, "Ripe Nashik bananas, sold by the dozen.");
        product(mahesh, "Mangoes", "Fruits", 120, "kg", 40, "🥭", 4.9, 201, "Alphonso mangoes, naturally ripened.");
        product(sita, "Fresh Milk", "Dairy", 56, "litre", 80, "🥛", 4.7, 92, "Pasteurised full-cream cow milk, delivered daily.");
        product(mahesh, "Turmeric Powder", "Spices", 120, "kg", 35, "🌶️", 4.8, 58, "Sun-dried and ground turmeric with a deep colour.");
        product(ramesh, "Sunflower Seeds", "Seeds", 90, "kg", 45, "🌻", 4.2, 21, "Raw sunflower seeds for snacking and sowing.");
    }

    private User user(String name, String email, String pw, User.Role role, String location) {
        User u = new User();
        u.name = name; u.email = email; u.password = encoder.encode(pw); u.role = role; u.location = location;
        u.phone = "98765 43210";
        return users.save(u);
    }

    private void product(User farmer, String name, String cat, int price, String unit, int stock, String emoji,
                         double rating, int reviews, String desc) {
        Product p = new Product();
        p.farmer = farmer; p.name = name; p.category = cat; p.price = BigDecimal.valueOf(price); p.unit = unit;
        p.stock = stock; p.emoji = emoji; p.rating = rating; p.reviews = reviews; p.description = desc;
        products.save(p);
    }
}
