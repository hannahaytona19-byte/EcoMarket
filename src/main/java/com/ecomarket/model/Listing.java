package com.ecomarket.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An item posted by a seller, mapped to the {@code products} table in schema.sql.
 *
 * <p>The database-backed fields are id, seller, category, title, description,
 * price, stockQuantity, condition, ecoBadge, carbonSavedKg, imageUrl, isActive
 * and createdAt. {@code location}, {@code tint}, {@code photoLabels},
 * {@code saved}, {@code status} and {@code postedAt} are prototype-only
 * presentation fields: meetup area, placeholder colors and the local
 * ACTIVE/SOLD listing lifecycle (not schema {@code orders.status}).</p>
 */
public class Listing {

    public enum Status { ACTIVE, SOLD }

    private String id;
    private User seller;
    private Category category;
    private String title;
    private double price;
    private ProductCondition condition;
    private int stockQuantity = 1;
    private String ecoBadge;
    private double carbonSavedKg;
    private String imageUrl;
    private boolean active = true;
    private String createdAt;

    private String location;
    private String description;
    private String tint = "#D9D9D9";
    private List<String> photoLabels = new ArrayList<>();
    private boolean saved;
    private Status status = Status.ACTIVE;
    private String postedAt;

    public Listing() {
    }

    public Listing(String id, User seller, Category category, String title, double price,
                   String condition, String location, String description, String tint,
                   String postedAt) {
        this(id, seller, category, title, price, ProductCondition.from(condition), location,
                description, tint, postedAt, 1, null, 0, null, postedAt);
    }

    public Listing(String id, User seller, Category category, String title, double price,
                   ProductCondition condition, String location, String description, String tint,
                   String postedAt, int stockQuantity, String ecoBadge, double carbonSavedKg,
                   String imageUrl, String createdAt) {
        this.id = id;
        this.seller = seller;
        this.category = category;
        this.title = title;
        this.price = price;
        this.condition = condition;
        this.stockQuantity = stockQuantity;
        this.ecoBadge = ecoBadge;
        this.carbonSavedKg = carbonSavedKg;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.location = location;
        this.description = description;
        this.tint = tint;
        this.postedAt = postedAt;
        this.photoLabels.add("Front view");
        this.photoLabels.add("Detail shot");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public ProductCondition getCondition() { return condition; }
    public void setCondition(ProductCondition condition) { this.condition = condition; }

    /** Convenience setter for forms that still parse a string value. */
    public void setCondition(String condition) {
        this.condition = ProductCondition.from(condition);
    }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getEcoBadge() { return ecoBadge; }
    public void setEcoBadge(String ecoBadge) { this.ecoBadge = ecoBadge; }

    public double getCarbonSavedKg() { return carbonSavedKg; }
    public void setCarbonSavedKg(double carbonSavedKg) { this.carbonSavedKg = carbonSavedKg; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTint() { return tint; }
    public void setTint(String tint) { this.tint = tint; }

    public List<String> getPhotoLabels() { return photoLabels; }
    public void setPhotoLabels(List<String> photoLabels) { this.photoLabels = photoLabels; }

    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getPostedAt() { return postedAt; }
    public void setPostedAt(String postedAt) { this.postedAt = postedAt; }

    @Override
    public String toString() {
        return title;
    }
}
