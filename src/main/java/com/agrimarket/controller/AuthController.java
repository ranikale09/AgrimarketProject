package com.agrimarket.controller;

import static com.agrimarket.controller.CurrentUser.bad;

import com.agrimarket.model.User;
import com.agrimarket.repo.UserRepository;
import com.agrimarket.security.JwtUtil;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    record RegisterReq(String name, String email, String password, String role, String phone, String location) {}
    record LoginReq(String email, String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterReq r) {
        if (r.name() == null || r.name().isBlank()) throw bad("Enter your name");
        if (r.email() == null || !r.email().contains("@")) throw bad("Enter a valid email address");
        if (r.password() == null || r.password().length() < 6) throw bad("Password must be at least 6 characters");
        if (users.existsByEmail(r.email().toLowerCase().trim())) throw bad("An account with this email already exists");
        User u = new User();
        u.name = r.name().trim();
        u.email = r.email().toLowerCase().trim();
        u.password = encoder.encode(r.password());
        u.phone = r.phone();
        u.location = r.location();
        u.role = "FARMER".equalsIgnoreCase(r.role()) ? User.Role.FARMER : User.Role.CUSTOMER;
        users.save(u);
        return Map.of("token", jwt.create(u.email, u.role.name()), "user", u);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginReq r) {
        User u = users.findByEmail(r.email() == null ? "" : r.email().toLowerCase().trim())
                .orElseThrow(() -> bad("Incorrect email or password"));
        if (!encoder.matches(r.password() == null ? "" : r.password(), u.password)) throw bad("Incorrect email or password");
        return Map.of("token", jwt.create(u.email, u.role.name()), "user", u);
    }
}
