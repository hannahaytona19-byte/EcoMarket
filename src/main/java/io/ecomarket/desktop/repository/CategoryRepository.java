package io.ecomarket.desktop.repository;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private static final Logger log = LoggerFactory.getLogger(CategoryRepository.class);

    private static final List<Category> FALLBACK_CATEGORIES = List.of(
            new Category(1, "Zero-Waste Kitchen", "zero-waste-kitchen", "Sustainable kitchenware", "kitchen"),
            new Category(2, "Eco Fashion & Apparel", "eco-fashion", "Upcycled organic cotton apparel", "shirt"),
            new Category(3, "Refurbished Tech", "refurbished-tech", "Certified refurbished gadgets", "devices"),
            new Category(4, "Personal Care", "personal-care", "Solid shampoo & plastic-free personal care", "spa")
    );

    public List<Category> findAll() {
        if (!DatabaseManager.isAvailable()) {
            return FALLBACK_CATEGORIES;
        }

        String sql = "SELECT category_id, name, slug, description, icon_name FROM categories ORDER BY name ASC";
        List<Category> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category c = new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("slug"),
                        rs.getString("description"),
                        rs.getString("icon_name")
                );
                list.add(c);
            }
            return list;
        } catch (Exception e) {
            log.error("Failed to query categories from Supabase: {}", e.getMessage());
            return FALLBACK_CATEGORIES;
        }
    }
}
