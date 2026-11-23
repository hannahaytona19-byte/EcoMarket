package com.ecomarket.model;

/**
 * Product condition values from {@code products.condition} in schema.sql.
 * Kept as an enum so the UI cannot introduce values the database CHECK
 * constraint would reject.
 */
public enum ProductCondition {

    NEW("New"),
    UPCYCLED("Upcycled"),
    REFURBISHED("Refurbished"),
    PRELOVED("Pre-loved");

    private final String label;

    ProductCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Accepts either an enum name or the friendly label shown in the UI. */
    public static ProductCondition from(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (ProductCondition condition : values()) {
            if (condition.name().equalsIgnoreCase(normalized)
                    || condition.label.equalsIgnoreCase(normalized)) {
                return condition;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
