package com.ecomarket.model;

/**
 * A browse category mapped to the {@code categories} table in schema.sql.
 * {@code icon} is the prototype-only glyph used when no image asset exists;
 * the other fields mirror the database columns.
 */
public class Category {

    private int id;
    private String name;
    private String slug;
    private String description;
    private String iconName;
    private String icon;

    public Category() {
    }

    public Category(int id, String name, String icon) {
        this(id, name, null, null, null, icon);
    }

    public Category(int id, String name, String slug, String description,
                    String iconName, String icon) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.iconName = iconName;
        this.icon = icon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @Override
    public String toString() {
        return name;
    }
}
