package io.ecomarket.desktop;

import io.ecomarket.desktop.schema.dto.CheckoutRequest;
import io.ecomarket.desktop.schema.dto.EcoImpactSummary;
import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.DeliveryMethod;
import io.ecomarket.desktop.schema.enums.OrderStatus;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import io.ecomarket.desktop.schema.enums.UserRole;
import io.ecomarket.desktop.service.CartService;
import io.ecomarket.desktop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private OrderService orderService;
    private User testBuyer;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        testBuyer = new User("u1", "Eco Tester", "tester@eco.io", "", UserRole.BUYER);
        testBuyer.setEcoPoints(50);
        cartService = new CartService(testBuyer);
    }

    @Test
    void testCheckoutEmptyCartThrowsException() {
        CheckoutRequest request = new CheckoutRequest(testBuyer.getUserId(), DeliveryMethod.GREEN_COURIER, "123 Eco St");
        EcoImpactSummary impact = new EcoImpactSummary(BigDecimal.ZERO, 0, BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () ->
                orderService.checkout(testBuyer, Collections.emptyList(), request, impact)
        );
    }

    @Test
    void testSuccessfulCheckoutAwardsPointsAndCreatesOrder() {
        Product p = new Product(
                "p1", testBuyer, new Category(1, "Solar", "solar", "", ""),
                "Solar Lamp", "Portable solar lantern",
                new BigDecimal("25.00"), 10, ProductCondition.NEW, "Clean Energy",
                new BigDecimal("12.00"), ""
        );
        cartService.addProduct(p, 1);

        CheckoutRequest request = new CheckoutRequest(testBuyer.getUserId(), DeliveryMethod.GREEN_COURIER, "123 Eco St");
        EcoImpactSummary impact = cartService.getImpactSummary();

        Order order = orderService.checkout(testBuyer, cartService.getCartItems(), request, impact);

        assertNotNull(order.getOrderId());
        assertTrue(order.getOrderNumber().startsWith("ECO-"));
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(DeliveryMethod.GREEN_COURIER, order.getDeliveryMethod());
        assertEquals(new BigDecimal("25.00"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());

        // Buyer points updated
        assertTrue(testBuyer.getEcoPoints() > 50);
    }
}
