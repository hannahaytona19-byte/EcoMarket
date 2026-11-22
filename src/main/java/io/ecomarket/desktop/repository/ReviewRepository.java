package io.ecomarket.desktop.repository;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.Review;
import io.ecomarket.desktop.schema.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReviewRepository {

    private static final Logger log = LoggerFactory.getLogger(ReviewRepository.class);
    private static final List<Review> FALLBACK_REVIEWS = new CopyOnWriteArrayList<>();

    static {
        User demoUser = new User("u0000001-0000-0000-0000-000000000001", "Eco Demo User", "buyer@ecomarket.io", "", null);
        Product wrap = new Product();
        wrap.setProductId("p0000001-0000-0000-0000-000000000001");

        FALLBACK_REVIEWS.add(new Review(
                "r0000001-0000-0000-0000-000000000001",
                wrap,
                demoUser,
                5,
                "Amazing quality beeswax wraps! They seal tightly over bowls and wash cleanly with cold water. Zero plastic needed!"
        ));
    }

    public List<Review> findByProductId(String productId) {
        if (!DatabaseManager.isAvailable()) {
            return FALLBACK_REVIEWS.stream()
                    .filter(r -> r.getProduct() != null && productId.equals(r.getProduct().getProductId()))
                    .toList();
        }

        String sql = """
            SELECT r.review_id, r.product_id, r.user_id, r.rating, r.comment, r.created_at,
                   u.full_name AS user_name, u.eco_points
            FROM reviews r
            JOIN users u ON r.user_id = u.user_id
            WHERE r.product_id = ?
            ORDER BY r.created_at DESC
        """;

        List<Review> reviews = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setFullName(rs.getString("user_name"));
                    user.setEcoPoints(rs.getInt("eco_points"));

                    Product product = new Product();
                    product.setProductId(productId);

                    Review review = new Review();
                    review.setReviewId(rs.getString("review_id"));
                    review.setProduct(product);
                    review.setUser(user);
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));

                    var ts = rs.getTimestamp("created_at");
                    if (ts != null) review.setCreatedAt(ts.toInstant());

                    reviews.add(review);
                }
            }
            return reviews;
        } catch (Exception e) {
            log.error("Failed to query reviews for product: {}", e.getMessage());
            return FALLBACK_REVIEWS.stream()
                    .filter(r -> r.getProduct() != null && productId.equals(r.getProduct().getProductId()))
                    .toList();
        }
    }

    public void addReview(Review review) {
        if (review.getReviewId() == null) {
            review.setReviewId(UUID.randomUUID().toString());
        }
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(Instant.now());
        }
        FALLBACK_REVIEWS.add(0, review);

        if (!DatabaseManager.isAvailable()) return;

        String sql = """
            INSERT INTO reviews (review_id, product_id, user_id, rating, comment)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (product_id, user_id) DO UPDATE SET
                rating = EXCLUDED.rating,
                comment = EXCLUDED.comment,
                created_at = NOW()
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, review.getReviewId());
            stmt.setString(2, review.getProduct().getProductId());
            stmt.setString(3, review.getUser().getUserId());
            stmt.setInt(4, review.getRating());
            stmt.setString(5, review.getComment());
            stmt.executeUpdate();
            log.info("Review posted for product {}", review.getProduct().getProductId());
        } catch (Exception e) {
            log.error("Failed to save review to Supabase: {}", e.getMessage());
        }
    }

    public double calculateAverageRating(String productId) {
        List<Review> list = findByProductId(productId);
        if (list.isEmpty()) return 0.0;
        return list.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
}
