package io.ecomarket.desktop.service;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.dto.CheckoutRequest;
import io.ecomarket.desktop.schema.dto.EcoImpactSummary;
import io.ecomarket.desktop.schema.entity.CartItem;
import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.OrderItem;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    public Order checkout(User buyer, List<CartItem> items, CheckoutRequest request, EcoImpactSummary impact) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart");
        }

        String orderId = UUID.randomUUID().toString();
        String orderNumber = "ECO-" + (100000 + RANDOM.nextInt(900000));

        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderNumber(orderNumber);
        order.setBuyer(buyer);
        order.setTotalAmount(impact.orderTotal());
        order.setTotalCarbonSavedKg(impact.totalCarbonSavedKg());
        order.setDeliveryMethod(request.deliveryMethod());
        order.setShippingAddress(request.shippingAddress());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(Instant.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : items) {
            OrderItem oi = new OrderItem(
                    UUID.randomUUID().toString(),
                    order,
                    item.getProduct(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            );
            orderItems.add(oi);
        }
        order.setItems(orderItems);

        // Update buyer eco-points
        buyer.setEcoPoints(buyer.getEcoPoints() + impact.ecoPointsEarned());

        // Persist to Supabase if available
        if (DatabaseManager.isAvailable()) {
            persistOrderToSupabase(order);
        }

        return order;
    }

    private void persistOrderToSupabase(Order order) {
        String insertOrderSql = """
            INSERT INTO orders (order_id, order_number, buyer_id, total_amount, total_carbon_saved_kg, delivery_method, shipping_address, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        String insertItemSql = """
            INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price)
            VALUES (?, ?, ?, ?, ?)
        """;
        String updateUserPointsSql = """
            UPDATE users SET eco_points = eco_points + ? WHERE user_id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql);
                 PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psUser = conn.prepareStatement(updateUserPointsSql)) {

                psOrder.setString(1, order.getOrderId());
                psOrder.setString(2, order.getOrderNumber());
                psOrder.setString(3, order.getBuyer().getUserId());
                psOrder.setBigDecimal(4, order.getTotalAmount());
                psOrder.setBigDecimal(5, order.getTotalCarbonSavedKg());
                psOrder.setString(6, order.getDeliveryMethod().name());
                psOrder.setString(7, order.getShippingAddress());
                psOrder.setString(8, order.getStatus().name());
                psOrder.executeUpdate();

                for (OrderItem oi : order.getItems()) {
                    psItem.setString(1, oi.getOrderItemId());
                    psItem.setString(2, order.getOrderId());
                    psItem.setString(3, oi.getProduct().getProductId());
                    psItem.setInt(4, oi.getQuantity());
                    psItem.setBigDecimal(5, oi.getUnitPrice());
                    psItem.addBatch();
                }
                psItem.executeBatch();

                conn.commit();
                log.info("Order {} persisted to Supabase successfully.", order.getOrderNumber());
            } catch (Exception e) {
                conn.rollback();
                log.error("Failed to commit order transaction to Supabase: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Database connection error while saving order: {}", e.getMessage());
        }
    }
}
