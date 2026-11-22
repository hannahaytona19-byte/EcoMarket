package io.ecomarket.desktop;

import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.OrderItem;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.DeliveryMethod;
import io.ecomarket.desktop.schema.enums.OrderStatus;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import io.ecomarket.desktop.schema.enums.UserRole;
import io.ecomarket.desktop.service.ReceiptExporter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptExporterTest {

    @Test
    void testHtmlReceiptGenerationContainsOrderAndCarbonDetails() {
        User buyer = new User("u1", "Green Hero", "hero@eco.io", "", UserRole.BUYER);
        Order order = new Order();
        order.setOrderId("ord-123");
        order.setOrderNumber("ECO-887766");
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("45.00"));
        order.setTotalCarbonSavedKg(new BigDecimal("9.20"));
        order.setDeliveryMethod(DeliveryMethod.GREEN_COURIER);
        order.setShippingAddress("Eco Haven 77");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(Instant.now());

        Product prod = new Product("p1", buyer, new Category(1, "Care", "care", "", ""),
                "Zero Waste Toothbrush", "Bamboo", new BigDecimal("15.00"), 10,
                ProductCondition.NEW, "Bio", new BigDecimal("3.00"), "");

        OrderItem item = new OrderItem("oi-1", order, prod, 3, new BigDecimal("15.00"));
        order.setItems(List.of(item));

        String html = ReceiptExporter.generateHtmlReceipt(order);

        assertNotNull(html);
        assertTrue(html.contains("ECO-887766"), "Should contain order number");
        assertTrue(html.contains("9.20 kg CO₂"), "Should display carbon saved certificate");
        assertTrue(html.contains("Green Hero"), "Should include customer name");
        assertTrue(html.contains("Zero Waste Toothbrush"), "Should list order items");
        assertTrue(html.contains("$45.00"), "Should list total amount");
    }
}
