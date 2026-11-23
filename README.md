# ecoMarket — JavaFX Desktop Application (Supabase Backend)

A sustainable e-commerce desktop application built with **Java 17+ / JavaFX 21** and backed by **Supabase (PostgreSQL)**.

## Architecture

```
                  ┌────────────────────────────────────────┐
                  │       ecoMarket JavaFX Desktop         │
                  │   (Controllers, FXML Views, CSS)       │
                  └──────────────────┬─────────────────────┘
                                     │
                   ┌─────────────────┴─────────────────┐
                   ▼                                   ▼
          ┌───────────────────┐               ┌──────────────────┐
          │  Supabase JDBC    │               │  Supabase REST   │
          │  HikariCP Pool    │               │  (PostgREST API) │
          └────────┬──────────┘               └────────┬─────────┘
                   │                                   │
                   └─────────────────┬─────────────────┘
                                     ▼
                  ┌────────────────────────────────────────┐
                  │           Supabase Cloud               │
                  │        (PostgreSQL Database)           │
                  └────────────────────────────────────────┘
```

## Quick Start

### 1. Database Setup (Supabase)
1. Go to your [Supabase Dashboard](https://supabase.com/dashboard).
2. Open the **SQL Editor**.
3. Run the schema script located in `supabase/schema.sql`. It creates:
   - `users`
   - `categories`
   - `products`
   - `cart_items`
   - `orders`
   - `order_items`
   - `reviews`
   - Indexes and sample eco-friendly seed data.

### 2. Configure Credentials
Copy `.env.example` to `.env` or edit `src/main/resources/supabase.properties`:

```properties
# Supabase PostgreSQL Connection
SUPABASE_DB_URL=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=YOUR_PASSWORD

# Supabase REST API (Optional / HTTP API)
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_ANON_KEY=YOUR_ANON_KEY
```

> **Note:** If no credentials are provided, the application runs automatically in **Offline / Local Demo Mode** using in-memory sample catalog data.

### 3. Run the Desktop App
```powershell
mvn clean javafx:run
```

> `javafx:run` now starts the **frontend-only prototype** (`com.ecomarket.app.Main`).
> The original backend-oriented sources under `io.ecomarket.desktop` are still
> present but are no longer the default launch target. If Maven is not on your
> PATH, use `.\mvnw.cmd javafx:run`. Full prototype details are in
> [`README_FRONTEND.md`](README_FRONTEND.md).

### Assumptions for the prototype screens
- **Schema alignment:** the active `com.ecomarket` prototype is offline, but its
  User/Category/Listing models, seed data, categories, conditions, stock, eco
  badges, carbon-saved values and image URLs now mirror `supabase/schema.sql`.
  Meetup location, favorites and ACTIVE/SOLD listing status remain UI-only.
- **Demo accounts:** schema seeds `buyer@ecomarket.io` / `password123` and
  `artisan@ecomarket.io` / `password123`; the former `juan@ecomarket.ph` and
  `elena@ecomarket.ph` aliases still work with `demo123`.
- **Profile:** initials-only avatar, schema-backed eco points and verified flag,
  and `Edit Profile` limited to first name, last name and location. Tabs show
  My Listings and Saved Items from in-memory data.
- **Seller Dashboard:** driven by the schema demo seller Eco Artisan Studio.
  “Unread Messages” is derived from unread in-app notifications, and CO₂ Saved
  sums active listings. Rows support Edit, Mark as Sold and Delete; “Post New
  Item” uses mock photo placeholders, not file uploads.

---

## Domain Model
- **Enums (`schema.enums`)**: `UserRole`, `ProductCondition`, `OrderStatus`, `DeliveryMethod`
- **Entities (`schema.entity`)**: `User`, `Category`, `Product`, `CartItem`, `Order`, `OrderItem`, `Review`
- **DTOs (`schema.dto`)**: `CheckoutRequest`, `EcoImpactSummary`
