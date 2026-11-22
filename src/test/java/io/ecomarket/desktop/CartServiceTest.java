package io.ecomarket.desktop;

import io.ecomarket.desktop.schema.dto.EcoImpactSummary;
import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import io.ecomarket.desktop.schema.enums.UserRole;
import io.ecomarket.desktop.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceTest {

    private CartService cartService;
    private Product sampleProduct1;
    private Product sampleProduct2;

    @BeforeEach
    void setUp() {
        User user = new User("u1", "Test Buyer", "test@eco.io", "", UserRole.BUYER);
        cartService = new CartService(user);

        sampleProduct1 = new Product(
                "p1", user, new Category(1, "Kitchen", "kitchen", "", ""),
                "Beeswax Wraps", "Reusable wrap",
                new BigDecimal("10.00"), 10, ProductCondition.NEW, "Eco",
                new BigDecimal("2.50"), ""
        );

        sampleProduct2 = new Product(
                "p2", user, new Category(1, "Kitchen", "kitchen", "", ""),
                "Denim Apron", "Upcycled apron",
                new BigDecimal("20.00"), 5, ProductCondition.UPCYCLED, "Upcycled",
                new BigDecimal("5.00"), ""
        );
    }

    @Test
    void testAddProductIncreasesQuantityAndTotal() {
        cartService.addProduct(sampleProduct1, 1);
        assertEquals(1, cartService.getCartItems().size());
        assertEquals(new BigDecimal("10.00"), cartService.getTotalAmount());
        assertEquals(new BigDecimal("2.50"), cartService.getTotalCarbonSaved());

        // Add same product again
        cartService.addProduct(sampleProduct1, 2);
        assertEquals(1, cartService.getCartItems().size());
        assertEquals(3, cartService.getCartItems().get(0).getQuantity());
        assertEquals(new BigDecimal("30.00"), cartService.getTotalAmount());
        assertEquals(new BigDecimal("7.50"), cartService.getTotalCarbonSaved());
    }

    @Test
    void testRemoveProduct() {
        cartService.addProduct(sampleProduct1, 1);
        cartService.addProduct(sampleProduct2, 1);
        assertEquals(2, cartService.getCartItems().size());

        cartService.removeProduct("p1");
        assertEquals(1, cartService.getCartItems().size());
        assertEquals("p2", cartService.getCartItems().get(0).getProduct().getProductId());
        assertEquals(new BigDecimal("20.00"), cartService.getTotalAmount());
    }

    @Test
    void testImpactSummaryCalculation() {
        cartService.addProduct(sampleProduct1, 2); // $20.00 total, 5.00 kg CO2 saved
        EcoImpactSummary summary = cartService.getImpactSummary();

        assertEquals(new BigDecimal("20.00"), summary.orderTotal());
        assertEquals(new BigDecimal("5.00"), summary.totalCarbonSavedKg());

        // 5.00 kg * 10 = 50 pts; $20 / 5 = 4 pts -> 54 pts
        assertEquals(54, summary.ecoPointsEarned());
    }
}
