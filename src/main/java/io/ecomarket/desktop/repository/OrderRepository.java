package io.ecomarket.desktop.repository;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.OrderItem;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.DeliveryMethod;
import io.ecomarket.desktop.schema.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);
    private static final List<Order> FALLBACK_ORDERS = new CopyOnWriteArrayList<>();

    public void recordOrder(Order order) {
        FALLBACK_ORDERS.add(0, order);
    }

    public List<Order> findByBuyerId(String buyerId) {
        if (!DatabaseManager.isAvailable()) {
            return FALLBACK_ORDERS.stream()
                    .filter(o -> o.getBuyer() != null && buyerId.equals(o.getBuyer().getUserId()))
                    .toList();
        }

        String sql = """
            SELECT o.order_id, o.order_number, o.buyer_id, o.total_amount,
                   o.total_carbon_saved_kg, o.delivery_method, o.shipping_address,
                   o.status, o.created_at,
                   u.full_name AS buyer_name, u.email AS buyer_email
            FROM orders o
            JOIN users u ON o.buyer_id = u.user_id
            WHERE o.buyer_id = ?
            ORDER BY o.created_at DESC
        """;

        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getString("order_id"));
                    order.setOrderNumber(rs.getString("order_number"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setTotalCarbonSavedKg(rs.getBigDecimal("total_carbon_saved_kg"));
                    order.setDeliveryMethod(DeliveryMethod.valueOf(rs.getString("delivery_method")));
                    order.setShippingAddress(rs.getString("shipping_address"));
                    order.setStatus(OrderStatus.valueOf(rs.getString("status")));

                    var ts = rs.getTimestamp("created_at");
                    if (ts != null) order.setCreatedAt(ts.toInstant());

                    User buyer = new User();
                    buyer.setUserId(rs.getString("buyer_id"));
                    buyer.setFullName(rs.getString("buyer_name"));
                    buyer.setEmail(rs.getString("buyer_email"));
                    order.setBuyer(buyer);

                    // Load items for each order
                    order.setItems(findItemsByOrderId(order.getOrderId(), conn));
                    orders.add(order);
                }
            }
            return orders;
        } catch (Exception e) {
            log.error("Failed to query orders for buyer: {}", e.getMessage());
            return FALLBACK_ORDERS.stream()
                    .filter(o -> o.getBuyer() != null && buyerId.equals(o.getBuyer().getUserId()))
                    .toList();
        }
    }

    private List<OrderItem> findItemsByOrderId(String orderId, Connection conn) {
        String itemSql = """
            SELECT oi.order_item_id, oi.quantity, oi.unit_price,
                   p.product_id, p.title, p.carbon_saved_kg, p.image_url
            FROM order_items oi
            JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_id = ?
        """;
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getString("product_id"));
                    p.setTitle(rs.getString("title"));
                    p.setCarbonSavedKg(rs.getBigDecimal("carbon_saved_kg"));
                    p.setImageUrl(rs.getString("image_url"));

                    OrderItem oi = new OrderItem();
                    oi.setOrderItemId(rs.getString("order_item_id"));
                    oi.setProduct(p);
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(oi);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load line items for order {}: {}", orderId, e.getMessage());
        }
        return items;
    }
}
