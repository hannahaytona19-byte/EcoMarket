package io.ecomarket.desktop.repository;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private static final Map<String, User> FALLBACK_USERS = new ConcurrentHashMap<>();

    static {
        User buyer = new User("u0000001-0000-0000-0000-000000000001", "Eco Demo User", "buyer@ecomarket.io", "password123", UserRole.BUYER);
        buyer.setEcoPoints(150);
        buyer.setAddress("Green Avenue 12, Metro City");
        buyer.setIsVerified(true);

        User seller = new User("u0000002-0000-0000-0000-000000000002", "Eco Artisan Studio", "artisan@ecomarket.io", "password123", UserRole.SELLER);
        seller.setEcoPoints(520);
        seller.setAddress("Sustainable Lane 44, Forest Hill");
        seller.setIsVerified(true);

        User admin = new User("u0000003-0000-0000-0000-000000000003", "Admin Supervisor", "admin@ecomarket.io", "admin123", UserRole.ADMIN);
        admin.setEcoPoints(1000);
        admin.setIsVerified(true);

        FALLBACK_USERS.put(buyer.getEmail().toLowerCase(), buyer);
        FALLBACK_USERS.put(seller.getEmail().toLowerCase(), seller);
        FALLBACK_USERS.put(admin.getEmail().toLowerCase(), admin);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();

        if (!DatabaseManager.isAvailable()) {
            return Optional.ofNullable(FALLBACK_USERS.get(email.toLowerCase().trim()));
        }

        String sql = """
            SELECT user_id, full_name, email, password_hash, role, phone_number,
                   address, avatar_url, eco_points, is_verified, created_at, updated_at
            FROM users WHERE LOWER(email) = LOWER(?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (Exception e) {
            log.error("Failed to find user by email in Supabase: {}", e.getMessage());
            return Optional.ofNullable(FALLBACK_USERS.get(email.toLowerCase().trim()));
        }
        return Optional.empty();
    }

    public Optional<User> findById(String userId) {
        if (userId == null) return Optional.empty();

        if (!DatabaseManager.isAvailable()) {
            return FALLBACK_USERS.values().stream()
                    .filter(u -> u.getUserId().equals(userId))
                    .findFirst();
        }

        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (Exception e) {
            log.error("Failed to find user by ID: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID().toString());
        }
        FALLBACK_USERS.put(user.getEmail().toLowerCase(), user);

        if (!DatabaseManager.isAvailable()) return;

        String sql = """
            INSERT INTO users (user_id, full_name, email, password_hash, role, phone_number, address, avatar_url, eco_points, is_verified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id) DO UPDATE SET
                full_name = EXCLUDED.full_name,
                email = EXCLUDED.email,
                role = EXCLUDED.role,
                phone_number = EXCLUDED.phone_number,
                address = EXCLUDED.address,
                avatar_url = EXCLUDED.avatar_url,
                eco_points = EXCLUDED.eco_points,
                is_verified = EXCLUDED.is_verified,
                updated_at = NOW()
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getPhoneNumber());
            stmt.setString(7, user.getAddress());
            stmt.setString(8, user.getAvatarUrl());
            stmt.setInt(9, user.getEcoPoints() != null ? user.getEcoPoints() : 0);
            stmt.setBoolean(10, user.getIsVerified() != null ? user.getIsVerified() : false);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to save user to Supabase: {}", e.getMessage());
        }
    }

    private User mapRow(ResultSet rs) throws Exception {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setAddress(rs.getString("address"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setEcoPoints(rs.getInt("eco_points"));
        user.setIsVerified(rs.getBoolean("is_verified"));

        var createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) user.setCreatedAt(createdTs.toInstant());

        var updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) user.setUpdatedAt(updatedTs.toInstant());

        return user;
    }
}
