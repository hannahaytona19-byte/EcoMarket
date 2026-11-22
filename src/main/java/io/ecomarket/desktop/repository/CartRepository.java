package io.ecomarket.desktop.repository;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.entity.CartItem;
import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    private static final Logger log = LoggerFactory.getLogger(CartRepository.class);

    public List<CartItem> findByUserId(String userId) {
        if (!DatabaseManager.isAvailable() || userId == null) {
            return new ArrayList<>();
        }

        String sql = """
            SELECT ci.cart_item_id, ci.quantity, ci.added_at,
                   p.product_id, p.title, p.price, p.carbon_saved_kg, p.condition, p.eco_badge, p.image_url,
                   c.category_id, c.name AS category_name
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.product_id
            JOIN categories c ON p.category_id = c.category_id
            WHERE ci.user_id = ?
            ORDER BY ci.added_at ASC
        """;

        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category cat = new Category();
                    cat.setCategoryId(rs.getInt("category_id"));
                    cat.setName(rs.getString("category_name"));

                    Product p = new Product();
                    p.setProductId(rs.getString("product_id"));
                    p.setTitle(rs.getString("title"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setCarbonSavedKg(rs.getBigDecimal("carbon_saved_kg"));
                    p.setCondition(ProductCondition.valueOf(rs.getString("condition")));
                    p.setEcoBadge(rs.getString("eco_badge"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setCategory(cat);

                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getString("cart_item_id"));
                    item.setProduct(p);
                    item.setQuantity(rs.getInt("quantity"));

                    var ts = rs.getTimestamp("added_at");
                    if (ts != null) item.setAddedAt(ts.toInstant());

                    items.add(item);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load cart items from Supabase: {}", e.getMessage());
        }
        return items;
    }

    public void upsertCartItem(String userId, String productId, int quantity) {
        if (!DatabaseManager.isAvailable() || userId == null || productId == null) return;

        String sql = """
            INSERT INTO cart_items (cart_item_id, user_id, product_id, quantity)
            VALUES (uuid_generate_v4()::text, ?, ?, ?)
            ON CONFLICT (user_id, product_id) DO UPDATE SET
                quantity = EXCLUDED.quantity,
                added_at = NOW()
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, productId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to upsert cart item in Supabase: {}", e.getMessage());
        }
    }

    public void removeCartItem(String userId, String productId) {
        if (!DatabaseManager.isAvailable() || userId == null || productId == null) return;

        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, productId);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to delete cart item: {}", e.getMessage());
        }
    }

    public void clearCart(String userId) {
        if (!DatabaseManager.isAvailable() || userId == null) return;

        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to clear cart: {}", e.getMessage());
        }
    }
}
