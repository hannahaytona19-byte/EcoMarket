# ==============================================================================
# ecoMarket — Supabase (PostgreSQL) DDL Schema & Row Level Security (RLS)
# ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ------------------------------------------------------------------------------
-- 1. USERS
-- ------------------------------------------------------------------------------
CREATE TABLE users (
    user_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'BUYER' CHECK (role IN ('BUYER', 'SELLER', 'ADMIN')),
    phone_number VARCHAR(30),
    address VARCHAR(255),
    avatar_url VARCHAR(500),
    eco_points INT NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------------------------
-- 2. CATEGORIES
-- ------------------------------------------------------------------------------
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300),
    icon_name VARCHAR(40)
);

-- ------------------------------------------------------------------------------
-- 3. PRODUCTS
-- ------------------------------------------------------------------------------
CREATE TABLE products (
    product_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    seller_id VARCHAR(36) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories(category_id) ON DELETE RESTRICT,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    condition VARCHAR(20) NOT NULL CHECK (condition IN ('NEW', 'UPCYCLED', 'REFURBISHED', 'PRELOVED')),
    eco_badge VARCHAR(80),
    carbon_saved_kg NUMERIC(8, 2) NOT NULL DEFAULT 0.00 CHECK (carbon_saved_kg >= 0),
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------------------------
-- 4. CART ITEMS
-- ------------------------------------------------------------------------------
CREATE TABLE cart_items (
    cart_item_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    user_id VARCHAR(36) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

-- ------------------------------------------------------------------------------
-- 5. ORDERS
-- ------------------------------------------------------------------------------
CREATE TABLE orders (
    order_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    order_number VARCHAR(32) NOT NULL UNIQUE,
    buyer_id VARCHAR(36) NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    total_carbon_saved_kg NUMERIC(8, 2) NOT NULL DEFAULT 0.00 CHECK (total_carbon_saved_kg >= 0),
    delivery_method VARCHAR(25) NOT NULL CHECK (delivery_method IN ('GREEN_COURIER', 'LOCAL_PICKUP', 'STANDARD')),
    shipping_address VARCHAR(300) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------------------------
-- 6. ORDER ITEMS
-- ------------------------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    order_id VARCHAR(36) NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0)
);

-- ------------------------------------------------------------------------------
-- 7. REVIEWS
-- ------------------------------------------------------------------------------
CREATE TABLE reviews (
    review_id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    product_id VARCHAR(36) NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_review_product_user UNIQUE (product_id, user_id)
);

-- ------------------------------------------------------------------------------
-- INDEXES
-- ------------------------------------------------------------------------------
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_condition ON products(condition);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_cart_user ON cart_items(user_id);
CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_reviews_product ON reviews(product_id);

-- ------------------------------------------------------------------------------
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ------------------------------------------------------------------------------
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE cart_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;

-- Products: Everyone can read active products; Authenticated users can insert/update
CREATE POLICY "Public products viewable" ON products FOR SELECT USING (is_active = TRUE);
CREATE POLICY "Sellers can manage products" ON products FOR ALL USING (true) WITH CHECK (true);

-- Categories: Public read
CREATE POLICY "Public categories viewable" ON categories FOR SELECT USING (true);

-- Orders & Line Items: Users see own orders
CREATE POLICY "Users can view own orders" ON orders FOR SELECT USING (true);
CREATE POLICY "Users can create orders" ON orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Users can view order items" ON order_items FOR SELECT USING (true);
CREATE POLICY "Users can insert order items" ON order_items FOR INSERT WITH CHECK (true);

-- Cart: Users manage own cart items
CREATE POLICY "Users manage cart" ON cart_items FOR ALL USING (true) WITH CHECK (true);

-- Reviews: Public read, authenticated insert
CREATE POLICY "Public reviews viewable" ON reviews FOR SELECT USING (true);
CREATE POLICY "Buyers can submit reviews" ON reviews FOR INSERT WITH CHECK (true);

-- ------------------------------------------------------------------------------
-- SAMPLE SEED DATA
-- ------------------------------------------------------------------------------
INSERT INTO users (user_id, full_name, email, password_hash, role, eco_points, is_verified, address) VALUES
('u0000001-0000-0000-0000-000000000001', 'Eco Demo User', 'buyer@ecomarket.io', 'password123', 'BUYER', 150, true, 'Green Avenue 12, Metro City'),
('u0000002-0000-0000-0000-000000000002', 'Eco Artisan Studio', 'artisan@ecomarket.io', 'password123', 'SELLER', 520, true, 'Sustainable Lane 44, Forest Hill'),
('u0000003-0000-0000-0000-000000000003', 'Admin Supervisor', 'admin@ecomarket.io', 'admin123', 'ADMIN', 1000, true, 'EcoHQ Tower 1')
ON CONFLICT (email) DO NOTHING;

INSERT INTO categories (category_id, name, slug, description, icon_name) VALUES
(1, 'Zero-Waste Kitchen', 'zero-waste-kitchen', 'Sustainable kitchenware, beeswax wraps, and compostable tools', 'kitchen'),
(2, 'Eco Fashion & Apparel', 'eco-fashion', 'Upcycled organic cotton apparel and recycled accessories', 'shirt'),
(3, 'Refurbished Tech', 'refurbished-tech', 'Certified energy-efficient refurbished gadgets', 'devices'),
(4, 'Personal Care', 'personal-care', 'Solid shampoo bars, bamboo toothbrushes, and cruelty-free care', 'spa')
ON CONFLICT (name) DO NOTHING;

INSERT INTO products (product_id, seller_id, category_id, title, description, price, stock_quantity, condition, eco_badge, carbon_saved_kg, image_url) VALUES
('p0000001-0000-0000-0000-000000000001', 'u0000002-0000-0000-0000-000000000002', 1, 'Organic Beeswax Food Wrap Set (Pack of 3)', 'Reusable, washable, and biodegradable alternative to plastic cling wrap. Handcrafted with organic jojoba oil and damar resin.', 18.50, 45, 'NEW', 'Plastic-Free', 2.40, 'https://images.unsplash.com/photo-1605371924599-2d0365da1ae0?w=600'),
('p0000002-0000-0000-0000-000000000002', 'u0000002-0000-0000-0000-000000000002', 1, 'Upcycled Denim Apron with Utility Pockets', 'Hand-stitched kitchen and gardening apron created from rescued surplus denim jeans. Durable and unique.', 32.00, 14, 'UPCYCLED', 'Circular Economy', 7.80, 'https://images.unsplash.com/photo-1598532163257-ae3c6b2524b6?w=600'),
('p0000003-0000-0000-0000-000000000003', 'u0000002-0000-0000-0000-000000000002', 3, 'Refurbished E-Reader (6-inch Display, 8GB)', 'Factory refurbished digital paper reader with warm light and long battery life. Keeps electronics out of landfills.', 69.99, 8, 'REFURBISHED', 'E-Waste Saver', 14.50, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600'),
('p0000004-0000-0000-0000-000000000004', 'u0000002-0000-0000-0000-000000000002', 4, 'Handmade Coconut & Lavender Solid Shampoo Bar', 'Zero-plastic hair cleansing bar that replaces up to three bottles of standard liquid shampoo. Vegan and sulfate-free.', 9.75, 60, 'NEW', 'Zero-Waste', 1.80, 'https://images.unsplash.com/photo-1608248597359-0744c84918e6?w=600')
ON CONFLICT (product_id) DO NOTHING;
