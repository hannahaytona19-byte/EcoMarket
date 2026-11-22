package io.ecomarket.desktop.schema.entity;

import io.ecomarket.desktop.schema.enums.DeliveryMethod;
import io.ecomarket.desktop.schema.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @Column(name = "order_number", length = 32, nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "total_carbon_saved_kg", precision = 8, scale = 2, nullable = false)
    private BigDecimal totalCarbonSavedKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 25, nullable = false)
    private DeliveryMethod deliveryMethod;

    @Column(name = "shipping_address", length = 300, nullable = false)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Order(
            String orderId,
            String orderNumber,
            User buyer,
            BigDecimal totalAmount,
            BigDecimal totalCarbonSavedKg,
            DeliveryMethod deliveryMethod,
            String shippingAddress
    ) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.buyer = buyer;
        this.totalAmount = totalAmount;
        this.totalCarbonSavedKg = totalCarbonSavedKg != null ? totalCarbonSavedKg : BigDecimal.ZERO;
        this.deliveryMethod = deliveryMethod;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalCarbonSavedKg() { return totalCarbonSavedKg; }
    public void setTotalCarbonSavedKg(BigDecimal totalCarbonSavedKg) { this.totalCarbonSavedKg = totalCarbonSavedKg; }

    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber='" + orderNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", deliveryMethod=" + deliveryMethod +
                '}';
    }
}
