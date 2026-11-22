package io.ecomarket.desktop.service;

import io.ecomarket.desktop.repository.CartRepository;
import io.ecomarket.desktop.schema.dto.EcoImpactSummary;
import io.ecomarket.desktop.schema.entity.CartItem;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class CartService {

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final CartRepository cartRepo = new CartRepository();
    private User currentUser;

    public CartService(User currentUser) {
        this.currentUser = currentUser;
        loadUserCart();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserCart();
    }

    public void loadUserCart() {
        cartItems.clear();
        if (currentUser != null) {
            List<CartItem> remoteItems = cartRepo.findByUserId(currentUser.getUserId());
            cartItems.addAll(remoteItems);
        }
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public void addProduct(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getProductId().equals(product.getProductId())) {
                int newQty = item.getQuantity() + quantity;
                item.setQuantity(newQty);
                int idx = cartItems.indexOf(item);
                cartItems.set(idx, item);

                if (currentUser != null) {
                    cartRepo.upsertCartItem(currentUser.getUserId(), product.getProductId(), newQty);
                }
                return;
            }
        }
        CartItem newItem = new CartItem(UUID.randomUUID().toString(), currentUser, product, quantity);
        cartItems.add(newItem);

        if (currentUser != null) {
            cartRepo.upsertCartItem(currentUser.getUserId(), product.getProductId(), quantity);
        }
    }

    public void removeProduct(String productId) {
        cartItems.removeIf(item -> item.getProduct().getProductId().equals(productId));
        if (currentUser != null) {
            cartRepo.removeCartItem(currentUser.getUserId(), productId);
        }
    }

    public void clear() {
        cartItems.clear();
        if (currentUser != null) {
            cartRepo.clearCart(currentUser.getUserId());
        }
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
        int pointsFromCarbon = carbon.multiply(BigDecimal.valueOf(10)).intValue();
        int pointsFromSpend = total.divide(BigDecimal.valueOf(5), RoundingMode.DOWN).intValue();
        int totalPoints = pointsFromCarbon + pointsFromSpend;

        return new EcoImpactSummary(carbon, totalPoints, total);
    }
}
