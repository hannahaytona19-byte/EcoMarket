package io.ecomarket.desktop.repository;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import io.ecomarket.desktop.schema.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);

    // Fallback seed products for local/offline testing
    private static final List<Product> FALLBACK_PRODUCTS = new CopyOnWriteArrayList<>();

    static {
        User seller = new User("u0000002-0000-0000-0000-000000000002", "Eco Artisan Studio", "artisan@ecomarket.io", "", UserRole.SELLER);
        Category cKitchen = new Category(1, "Zero-Waste Kitchen", "zero-waste-kitchen", "Sustainable kitchenware", "kitchen");
        Category cFashion = new Category(2, "Eco Fashion & Apparel", "eco-fashion", "Upcycled organic cotton", "shirt");
        Category cTech = new Category(3, "Refurbished Tech", "refurbished-tech", "Energy-efficient refurbished gadgets", "devices");
        Category cCare = new Category(4, "Personal Care", "personal-care", "Solid shampoo & plastic-free personal care", "spa");

        FALLBACK_PRODUCTS.add(new Product(
                "p0000001-0000-0000-0000-000000000001", seller, cKitchen,
                "Organic Beeswax Food Wrap Set (Pack of 3)",
                "Reusable, washable, and biodegradable alternative to plastic cling wrap. Handcrafted with organic jojoba oil and damar resin.",
                new BigDecimal("18.50"), 45, ProductCondition.NEW, "Plastic-Free", new BigDecimal("2.40"),
                "https://images.unsplash.com/photo-1605371924599-2d0365da1ae0?w=600"
        ));
        FALLBACK_PRODUCTS.add(new Product(
                "p0000002-0000-0000-0000-000000000002", seller, cKitchen,
                "Upcycled Denim Apron with Utility Pockets",
                "Hand-stitched kitchen and gardening apron created from rescued surplus denim jeans. Durable and unique.",
                new BigDecimal("32.00"), 14, ProductCondition.UPCYCLED, "Circular Economy", new BigDecimal("7.80"),
                "https://images.unsplash.com/photo-1598532163257-ae3c6b2524b6?w=600"
        ));
        FALLBACK_PRODUCTS.add(new Product(
                "p0000003-0000-0000-0000-000000000003", seller, cTech,
                "Refurbished E-Reader (6-inch Display, 8GB)",
                "Factory refurbished digital paper reader with warm light and long battery life. Keeps electronics out of landfills.",
                new BigDecimal("69.99"), 8, ProductCondition.REFURBISHED, "E-Waste Saver", new BigDecimal("14.50"),
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600"
        ));
        FALLBACK_PRODUCTS.add(new Product(
                "p0000004-0000-0000-0000-000000000004", seller, cCare,
                "Handmade Coconut & Lavender Solid Shampoo Bar",
                "Zero-plastic hair cleansing bar that replaces up to three bottles of standard liquid shampoo. Vegan and sulfate-free.",
                new BigDecimal("9.75"), 60, ProductCondition.NEW, "Zero-Waste", new BigDecimal("1.80"),
                "https://images.unsplash.com/photo-1608248597359-0744c84918e6?w=600"
        ));
    }

    public List<Product> findAllActive() {
        if (!DatabaseManager.isAvailable()) {
            return new ArrayList<>(FALLBACK_PRODUCTS);
        }

        String sql = """
            SELECT p.product_id, p.title, p.description, p.price, p.stock_quantity,
                   p.condition, p.eco_badge, p.carbon_saved_kg, p.image_url, p.is_active,
                   c.category_id, c.name AS category_name, c.slug AS category_slug,
                   u.user_id AS seller_id, u.full_name AS seller_name, u.email AS seller_email
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            JOIN users u ON p.seller_id = u.user_id
            WHERE p.is_active = TRUE
            ORDER BY p.created_at DESC
        """;

        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = mapRow(rs);
                products.add(product);
            }
            return products;
        } catch (Exception e) {
            log.error("Failed to query Supabase products: {}", e.getMessage());
            return new ArrayList<>(FALLBACK_PRODUCTS);
        }
    }

    public List<Product> filter(String keyword, Integer categoryId, ProductCondition condition) {
        List<Product> all = findAllActive();
        return all.stream()
                .filter(p -> keyword == null || keyword.isBlank() ||
                        p.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .filter(p -> categoryId == null || (p.getCategory() != null && categoryId.equals(p.getCategory().getCategoryId())))
                .filter(p -> condition == null || condition.equals(p.getCondition()))
                .toList();
    }

    public void save(Product product) {
        if (product.getProductId() == null || product.getProductId().isBlank()) {
            product.setProductId(java.util.UUID.randomUUID().toString());
        }
        FALLBACK_PRODUCTS.removeIf(p -> p.getProductId().equals(product.getProductId()));
        FALLBACK_PRODUCTS.add(0, product);

        if (!DatabaseManager.isAvailable()) return;

        String sql = """
            INSERT INTO products (product_id, seller_id, category_id, title, description, price, stock_quantity, condition, eco_badge, carbon_saved_kg, image_url, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (product_id) DO UPDATE SET
                title = EXCLUDED.title,
                description = EXCLUDED.description,
                price = EXCLUDED.price,
                stock_quantity = EXCLUDED.stock_quantity,
                condition = EXCLUDED.condition,
                eco_badge = EXCLUDED.eco_badge,
                carbon_saved_kg = EXCLUDED.carbon_saved_kg,
                image_url = EXCLUDED.image_url,
                is_active = EXCLUDED.is_active
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getProductId());
            stmt.setString(2, product.getSeller().getUserId());
            stmt.setInt(3, product.getCategory().getCategoryId());
            stmt.setString(4, product.getTitle());
            stmt.setString(5, product.getDescription());
            stmt.setBigDecimal(6, product.getPrice());
            stmt.setInt(7, product.getStockQuantity());
            stmt.setString(8, product.getCondition().name());
            stmt.setString(9, product.getEcoBadge());
            stmt.setBigDecimal(10, product.getCarbonSavedKg());
            stmt.setString(11, product.getImageUrl());
            stmt.setBoolean(12, product.getIsActive() != null ? product.getIsActive() : true);
            stmt.executeUpdate();
            log.info("Product {} saved successfully to Supabase.", product.getTitle());
        } catch (Exception e) {
            log.error("Failed to save product to Supabase: {}", e.getMessage());
        }
    }

    private Product mapRow(ResultSet rs) throws Exception {
        User seller = new User();
        seller.setUserId(rs.getString("seller_id"));
        seller.setFullName(rs.getString("seller_name"));
        seller.setEmail(rs.getString("seller_email"));

        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setSlug(rs.getString("category_slug"));

        Product p = new Product();
        p.setProductId(rs.getString("product_id"));
        p.setSeller(seller);
        p.setCategory(category);
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setCondition(ProductCondition.valueOf(rs.getString("condition")));
        p.setEcoBadge(rs.getString("eco_badge"));
        p.setCarbonSavedKg(rs.getBigDecimal("carbon_saved_kg"));
        p.setImageUrl(rs.getString("image_url"));
        p.setIsActive(rs.getBoolean("is_active"));
        return p;
    }
}
