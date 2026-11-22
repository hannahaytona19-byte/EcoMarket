package io.ecomarket.desktop.schema.entity;

import io.ecomarket.desktop.schema.enums.ProductCondition;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", length = 160, nullable = false)
    private String title;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", length = 20, nullable = false)
    private ProductCondition condition;

    @Column(name = "eco_badge", length = 80)
    private String ecoBadge;

    @Column(name = "carbon_saved_kg", precision = 8, scale = 2, nullable = false)
    private BigDecimal carbonSavedKg = BigDecimal.ZERO;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public Product() {
        this.isActive = true;
        this.carbonSavedKg = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public Product(
            String productId,
            User seller,
            Category category,
            String title,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            ProductCondition condition,
            String ecoBadge,
            BigDecimal carbonSavedKg,
            String imageUrl
    ) {
        this.productId = productId;
        this.seller = seller;
        this.category = category;
        this.title = title;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.condition = condition;
        this.ecoBadge = ecoBadge;
        this.carbonSavedKg = carbonSavedKg != null ? carbonSavedKg : BigDecimal.ZERO;
        this.imageUrl = imageUrl;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public ProductCondition getCondition() { return condition; }
    public void setCondition(ProductCondition condition) { this.condition = condition; }

    public String getEcoBadge() { return ecoBadge; }
    public void setEcoBadge(String ecoBadge) { this.ecoBadge = ecoBadge; }

    public BigDecimal getCarbonSavedKg() { return carbonSavedKg; }
    public void setCarbonSavedKg(BigDecimal carbonSavedKg) { this.carbonSavedKg = carbonSavedKg; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", condition=" + condition +
                ", carbonSavedKg=" + carbonSavedKg +
                '}';
    }
}
