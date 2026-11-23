package com.ecomarket;

import com.ecomarket.model.Category;
import com.ecomarket.model.Listing;
import com.ecomarket.model.ProductCondition;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the mapping between the offline prototype seed and supabase/schema.sql. */
class SchemaAlignmentTest {

    private final MockDataService service = MockDataService.getInstance();

    @Test
    void seedCategoriesMatchSchemaRows() {
        List<Category> categories = service.getCategories();
        assertEquals(4, categories.size());
        assertEquals("Zero-Waste Kitchen", categories.get(0).getName());
        assertEquals("eco-fashion", categories.get(1).getSlug());
        assertEquals("devices", categories.get(2).getIconName());
        assertEquals("personal-care", categories.get(3).getSlug());
    }

    @Test
    void seedProductsUseSchemaFieldsAndConditions() {
        List<Listing> listings = service.getListings();
        assertEquals(4, listings.size());
        for (Listing listing : listings) {
            assertNotNull(listing.getCondition());
            assertTrue(listing.getStockQuantity() > 0);
            assertTrue(listing.getCarbonSavedKg() > 0);
            assertNotNull(listing.getEcoBadge());
            assertNotNull(listing.getImageUrl());
            assertEquals("2026-11-23", listing.getCreatedAt());
            assertTrue(listing.isActive());
        }
        assertEquals(ProductCondition.UPCYCLED, listings.get(1).getCondition());
        assertEquals("E-Waste Saver", listings.get(2).getEcoBadge());
    }

    @Test
    void seedUsersMatchSchemaRolesAndEcoPoints() {
        assertEquals(User.Role.BUYER, service.demoBuyer().getRole());
        assertEquals(150, service.demoBuyer().getEcoPoints());
        assertTrue(service.demoBuyer().isVerified());
        assertEquals("Eco Artisan Studio", service.demoSeller().getFullName());
        assertEquals(User.Role.SELLER, service.demoSeller().getRole());
        assertEquals(520, service.demoSeller().getEcoPoints());
    }

    @Test
    void conditionEnumAcceptsOnlySchemaValues() {
        assertNotNull(ProductCondition.from("PRELOVED"));
        assertNotNull(ProductCondition.from("Upcycled"));
        assertEquals(ProductCondition.NEW, ProductCondition.from("New"));
        assertFalse(ProductCondition.values().length != 4);
    }

    @Test
    void legacyDemoEmailAliasesStillResolve() {
        assertTrue(service.authenticate("juan@ecomarket.ph", "demo123").isPresent());
        assertEquals(service.demoBuyer(), service.getCurrentUser());
        assertTrue(service.authenticate("elena@ecomarket.ph", "demo123").isPresent());
        assertEquals(service.demoSeller(), service.getCurrentUser());
        service.enterAsBuyer();
    }
}
