package io.ecomarket.desktop.ui.dialog;

import io.ecomarket.desktop.repository.OrderRepository;
import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.OrderItem;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.service.ReceiptExporter;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class OrdersDialog extends Dialog<Void> {

    public OrdersDialog(User buyer, OrderRepository orderRepo) {
        setTitle("My Eco-Orders & Purchase History");
        setHeaderText("Track your eco-purchases and total environmental impact");
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setPrefWidth(600);

        List<Order> orders = orderRepo.findByBuyerId(buyer.getUserId());

        BigDecimal totalCarbonAll = orders.stream()
                .map(Order::getTotalCarbonSavedKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lifetime carbon impact banner
        HBox impactBanner = new HBox(12);
        impactBanner.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #a5d6a7;");
        Label carbonIcon = new Label("🌱");
        carbonIcon.setStyle("-fx-font-size: 24px;");
        VBox bannerText = new VBox(2);
        Label bannerTitle = new Label("Lifetime Eco-Contribution");
        bannerTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        Label bannerDetail = new Label(String.format("You have prevented %.2f kg of CO2 from entering the atmosphere through %d orders!", totalCarbonAll, orders.size()));
        bannerDetail.setStyle("-fx-font-size: 11px; -fx-text-fill: #2e7d32;");
        bannerText.getChildren().addAll(bannerTitle, bannerDetail);
        impactBanner.getChildren().addAll(carbonIcon, bannerText);

        container.getChildren().add(impactBanner);

        VBox orderList = new VBox(10);
        ScrollPane scroll = new ScrollPane(orderList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(340);

        if (orders.isEmpty()) {
            Label empty = new Label("No orders found. Shop sustainable goods to track your carbon footprint!");
            empty.setStyle("-fx-text-fill: #757575; -fx-padding: 20;");
            orderList.getChildren().add(empty);
        } else {
            for (Order o : orders) {
                VBox card = new VBox(6);
                card.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 6;");

                HBox cardHeader = new HBox(10);
                Label num = new Label(o.getOrderNumber());
                num.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20;");

                Label statusBadge = new Label(o.getStatus().name());
                statusBadge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px;");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Label total = new Label(String.format("$%.2f", o.getTotalAmount()));
                total.setStyle("-fx-font-weight: bold;");

                cardHeader.getChildren().addAll(num, statusBadge, sp, total);

                HBox cardFooter = new HBox(10);
                cardFooter.setStyle("-fx-alignment: center-left; -fx-padding: 4 0 0 0;");
                Label details = new Label(String.format("Delivery: %s  |  CO2 Saved: %.2f kg", o.getDeliveryMethod(), o.getTotalCarbonSavedKg()));
                details.setStyle("-fx-font-size: 11px; -fx-text-fill: #616161;");
                HBox.setHgrow(details, Priority.ALWAYS);

                Button exportBtn = new Button("📄 Eco-Receipt & Certificate");
                exportBtn.setStyle("-fx-background-color: #f1f8e9; -fx-text-fill: #2e7d32; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #a5d6a7; -fx-border-radius: 4; -fx-cursor: hand;");
                exportBtn.setOnAction(e -> {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Save Eco-Receipt");
                    chooser.setInitialFileName("ecoMarket_Receipt_" + o.getOrderNumber() + ".html");
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Document (*.html)", "*.html"));
                    File target = chooser.showSaveDialog(getDialogPane().getScene().getWindow());
                    if (target != null) {
                        try {
                            ReceiptExporter.saveReceiptToFile(o, target);
                            Alert a = new Alert(Alert.AlertType.INFORMATION, "Receipt saved successfully to " + target.getName());
                            a.showAndWait();
                        } catch (Exception ex) {
                            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to save receipt: " + ex.getMessage());
                            a.showAndWait();
                        }
                    }
                });
                cardFooter.getChildren().addAll(details, exportBtn);

                // Line items
                VBox itemsBox = new VBox(2);
                for (OrderItem item : o.getItems()) {
                    Label line = new Label(String.format("  • %s  x%d  ($%.2f)",
                            item.getProduct() != null ? item.getProduct().getTitle() : "Item",
                            item.getQuantity(),
                            item.getUnitPrice()));
                    line.setStyle("-fx-font-size: 11px; -fx-text-fill: #424242;");
                    itemsBox.getChildren().add(line);
                }

                card.getChildren().addAll(cardHeader, cardFooter, itemsBox);
                orderList.getChildren().add(card);
            }
        }

        container.getChildren().add(scroll);
        getDialogPane().setContent(container);
    }
}
