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

---

## Domain Model
- **Enums (`schema.enums`)**: `UserRole`, `ProductCondition`, `OrderStatus`, `DeliveryMethod`
- **Entities (`schema.entity`)**: `User`, `Category`, `Product`, `CartItem`, `Order`, `OrderItem`, `Review`
- **DTOs (`schema.dto`)**: `CheckoutRequest`, `EcoImpactSummary`
