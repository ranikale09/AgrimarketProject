# AgriMarket

Farm-to-home grocery marketplace.
Frontend: HTML, CSS, JavaScript (no framework). Backend: Spring Boot 3 (Java 17), Spring Security + JWT, Spring Data JPA. Database: MySQL.

## Run it

1. Install JDK 17+, Maven 3.9+ and MySQL 8.
2. Edit `src/main/resources/application.properties`: set `spring.datasource.password` and a long random `agrimarket.jwt.secret`.
3. Start the app (the `agrimarket` database and all tables are created automatically):
   ```
   mvn spring-boot:run
   ```
4. Open http://localhost:8080

The frontend lives in `src/main/resources/static` and is served by Spring Boot, so the pages and API share one origin (no CORS setup needed).

On first start with an empty database, demo data is seeded (`DataSeeder.java`):

| Role     | Email                    | Password    |
|----------|--------------------------|-------------|
| Customer | rani@example.com         | password123 |
| Farmer   | ramesh@agrimarket.com    | password123 |
| Admin    | admin@agrimarket.com     | admin123    |

## Pages

| Page | What it does |
|------|--------------|
| `index.html` | Home: hero, categories, featured products |
| `products.html` | Listing with category, price, stock filters, search, sorting |
| `product.html?id=` | Product detail, farmer card, quantity, add to cart |
| `cart.html` | Change quantities, remove items, order summary |
| `checkout.html` | Address + payment method (cash or UPI on delivery), places the order |
| `orders.html` | Order history with status tracking timeline |
| `login.html` | Log in / create account (customer or farmer) |
| `dashboard.html` | Customer, farmer (products CRUD, order status) and admin views |

## REST API

| Method | Path | Access |
|--------|------|--------|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/products?category=&q=&sort=&inStock=`, `/api/products/{id}` | public |
| GET/POST | `/api/cart`; PUT/DELETE `/api/cart/{id}` | logged in |
| GET/POST | `/api/orders`; GET `/api/orders/{id}` | logged in |
| GET/POST | `/api/farmer/products`; PUT/DELETE `/api/farmer/products/{id}` | farmer |
| GET | `/api/farmer/orders`, `/api/farmer/stats`; PUT `/api/farmer/orders/{id}/status` | farmer |
| GET | `/api/admin/stats`, `/api/admin/users` | admin |

Send `Authorization: Bearer <token>` on protected calls.

## Project layout

```
src/main/java/com/agrimarket
  model/        User, Product, CartItem, Order, OrderItem (JPA entities)
  repo/         Spring Data repositories
  security/     JwtUtil, JwtFilter, SecurityConfig
  controller/   Auth, Product, Cart, Order, Farmer, Admin
  DataSeeder.java
src/main/resources
  application.properties
  static/       HTML pages, css/style.css, js/app.js
```

## Notes before going live

- Payments are recorded as a method only (cash/UPI on delivery). Add a gateway such as Razorpay for online payments.
- Product images use emoji. Add an `imageUrl` column and an upload endpoint to use photos.
- Keep the JWT secret and DB password out of source control (use environment variables).
- Switch `spring.jpa.hibernate.ddl-auto` from `update` to `validate` and use Flyway migrations in production.
