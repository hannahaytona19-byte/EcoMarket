package io.ecomarket.desktop.service;

import io.ecomarket.desktop.schema.dto.EcoImpactSummary;
import io.ecomarket.desktop.schema.entity.CartItem;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class CartService {

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final User currentUser;

    public CartService(User currentUser) {
        this.currentUser = currentUser;
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public void addProduct(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getProductId().equals(product.getProductId())) {
                item.setQuantity(item.getQuantity() + quantity);
                // Trigger change notification
                int idx = cartItems.indexOf(item);
                cartItems.set(idx, item);
                return;
            }
        }
        CartItem newItem = new CartItem(UUID.randomUUID().toString(), currentUser, product, quantity);
        cartItems.add(newItem);
    }

    public void removeProduct(String productId) {
        cartItems.removeIf(item -> item.getProduct().getProductId().equals(productId));
    }

    public void clear() {
        cartItems.clear();
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal price = item.getProduct().getPrice();
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            total = total.add(price.multiply(qty));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalCarbonSaved() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal saved = item.getProduct().getCarbonSavedKg();
            if (saved != null) {
                total = total.add(saved.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public EcoImpactSummary getImpactSummary() {
        BigDecimal carbon = getTotalCarbonSaved();
        BigDecimal total = getTotalAmount();
        // 10 eco points per 1.00 kg CO2 saved + 1 point per $5 spent
        int pointsFromCarbon = carbon.multiply(BigDecimal.valueOf(10)).intValue();
        int pointsFromSpend = total.divide(BigDecimal.valueOf(5), RoundingMode.DOWN).intValue();
        int totalPoints = pointsFromCarbon + pointsFromSpend;

        return new EcoImpactSummary(carbon, totalPoints, total);
    }
}
