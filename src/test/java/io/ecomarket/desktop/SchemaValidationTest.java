package io.ecomarket.desktop;

import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.Review;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaValidationTest {

    @Test
    void testReviewRatingConstraints() {
        User user = new User("u1", "Reviewer", "rev@eco.io", "", UserRole.BUYER);
        Product product = new Product();
        product.setProductId("p1");

        // Rating within 1..5 should succeed
        assertDoesNotThrow(() -> new Review("r1", product, user, 5, "Excellent!"));
        assertDoesNotThrow(() -> new Review("r2", product, user, 1, "Disappointed"));

        // Rating outside 1..5 should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> new Review("r3", product, user, 0, "Too low"));
        assertThrows(IllegalArgumentException.class, () -> new Review("r4", product, user, 6, "Too high"));
    }

    @Test
    void testUserEqualityBasedOnId() {
        User u1 = new User("same-id", "Name A", "a@eco.io", "", UserRole.BUYER);
        User u2 = new User("same-id", "Name B", "b@eco.io", "", UserRole.SELLER);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }
}
