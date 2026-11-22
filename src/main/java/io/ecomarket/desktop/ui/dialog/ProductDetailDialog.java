package io.ecomarket.desktop.ui.dialog;

import io.ecomarket.desktop.repository.ReviewRepository;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.Review;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProductDetailDialog extends Dialog<Void> {

    private final Product product;
    private final ReviewRepository reviewRepo;
    private final VBox reviewsListBox = new VBox(8);
    private final Label avgRatingLabel = new Label();

    public ProductDetailDialog(Product product, ReviewRepository reviewRepo) {
        this.product = product;
        this.reviewRepo = reviewRepo;

        setTitle("ecoMarket — " + product.getTitle());
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setPrefWidth(550);

        // Product Header
        Label title = new Label(product.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");

        HBox badgeBar = new HBox(10);
        Label badge = new Label("🌱 " + (product.getEcoBadge() != null ? product.getEcoBadge() : product.getCondition().name()));
        badge.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 4 8; -fx-font-weight: bold; -fx-background-radius: 6;");

        Label carbonBadge = new Label("🌍 Saves " + product.getCarbonSavedKg() + " kg CO2");
        carbonBadge.setStyle("-fx-background-color: #e0f2f1; -fx-text-fill: #00796b; -fx-padding: 4 8; -fx-font-weight: bold; -fx-background-radius: 6;");

        Label conditionBadge = new Label("Condition: " + product.getCondition());
        conditionBadge.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 4 8; -fx-background-radius: 6;");

        badgeBar.getChildren().addAll(badge, carbonBadge, conditionBadge);

        Label price = new Label(String.format("Price: $%.2f  |  In Stock: %d", product.getPrice(), product.getStockQuantity()));
        price.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        Label desc = new Label(product.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #424242;");

        Separator sep = new Separator();

        // Reviews section
        Label reviewHeader = new Label("Customer Reviews & Eco Feedback");
        reviewHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        avgRatingLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f57f17;");

        ScrollPane reviewScroll = new ScrollPane(reviewsListBox);
        reviewScroll.setFitToWidth(true);
        reviewScroll.setPrefHeight(150);

        // Add review form
        VBox addReviewBox = new VBox(6);
        addReviewBox.setStyle("-fx-background-color: #f9fbf9; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 6;");

        Label writeLabel = new Label("Write a Review:");
        writeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        HBox ratingInputBar = new HBox(8);
        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
        ratingCombo.setValue(5);

        TextField commentField = new TextField();
        commentField.setPromptText("Share your experience with this sustainable product...");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button submitBtn = new Button("Submit Review");
        submitBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        submitBtn.setOnAction(e -> {
            String comment = commentField.getText();
            if (comment != null && !comment.isBlank()) {
                User user = AuthService.getInstance().getCurrentUser();
                Review r = new Review(null, product, user, ratingCombo.getValue(), comment);
                reviewRepo.addReview(r);
                commentField.clear();
                refreshReviews();
            }
        });

        ratingInputBar.getChildren().addAll(new Label("Rating:"), ratingCombo, commentField, submitBtn);
        addReviewBox.getChildren().addAll(writeLabel, ratingInputBar);

        root.getChildren().addAll(title, badgeBar, price, desc, sep, reviewHeader, avgRatingLabel, reviewScroll, addReviewBox);
        getDialogPane().setContent(root);

        refreshReviews();
    }

    private void refreshReviews() {
        reviewsListBox.getChildren().clear();
        List<Review> list = reviewRepo.findByProductId(product.getProductId());
        double avg = reviewRepo.calculateAverageRating(product.getProductId());

        if (list.isEmpty()) {
            avgRatingLabel.setText("No reviews yet. Be the first to share your thoughts!");
        } else {
            avgRatingLabel.setText(String.format("Average Rating: ★ %.1f / 5.0 (%d reviews)", avg, list.size()));
        }

        for (Review r : list) {
            VBox card = new VBox(3);
            card.setStyle("-fx-background-color: #ffffff; -fx-padding: 8; -fx-border-color: #e8e8e8; -fx-border-radius: 4;");

            String stars = "★".repeat(Math.max(1, r.getRating())) + "☆".repeat(Math.max(0, 5 - r.getRating()));
            Label userLabel = new Label((r.getUser() != null ? r.getUser().getFullName() : "Verified Buyer") + "  " + stars);
            userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            Label commentLabel = new Label(r.getComment());
            commentLabel.setWrapText(true);
            commentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

            card.getChildren().addAll(userLabel, commentLabel);
            reviewsListBox.getChildren().add(card);
        }
    }
}
