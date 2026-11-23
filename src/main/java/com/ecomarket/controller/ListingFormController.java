package com.ecomarket.controller;

import com.ecomarket.model.Category;
import com.ecomarket.model.Listing;
import com.ecomarket.model.ProductCondition;
import com.ecomarket.service.MockDataService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Shared create/edit dialog. Photo slots are clickable placeholders only. */
public class ListingFormController {

    @FXML private Label formTitle;
    @FXML private HBox photoSlots;
    @FXML private TextField titleField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private ComboBox<ProductCondition> conditionCombo;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField carbonField;
    @FXML private TextField locationField;
    @FXML private TextField ecoBadgeField;
    @FXML private TextField imageUrlField;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final MockDataService service = MockDataService.getInstance();
    private Listing existing;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        categoryCombo.getItems().addAll(service.getCategories());
        conditionCombo.getItems().addAll(ProductCondition.values());
        for (int i = 1; i <= 4; i++) {
            final int slotNumber = i;
            Label caption = new Label("Photo " + slotNumber + "\n+ Add");
            caption.getStyleClass().add("placeholder-caption");
            StackPane slot = new StackPane(caption);
            slot.getStyleClass().add("photo-slot");
            slot.setOnMouseClicked(e -> {
                boolean added = caption.getText().contains("✓");
                caption.setText(added
                        ? "Photo " + slotNumber + "\n+ Add"
                        : "Photo " + slotNumber + "\n✓ Added");
            });
            photoSlots.getChildren().add(slot);
        }
    }

    /** Called by UiUtil after the FXML is loaded. */
    public void setListing(Listing listing, Runnable onSavedCallback) {
        this.existing = listing;
        this.onSaved = onSavedCallback;
        if (listing == null) return;

        formTitle.setText("Edit Listing");
        saveButton.setText("Save Changes");
        titleField.setText(listing.getTitle());
        categoryCombo.setValue(listing.getCategory());
        conditionCombo.setValue(listing.getCondition());
        priceField.setText(String.valueOf(listing.getPrice()));
        stockField.setText(String.valueOf(listing.getStockQuantity()));
        carbonField.setText(String.valueOf(listing.getCarbonSavedKg()));
        locationField.setText(listing.getLocation());
        ecoBadgeField.setText(listing.getEcoBadge() == null ? "" : listing.getEcoBadge());
        imageUrlField.setText(listing.getImageUrl() == null ? "" : listing.getImageUrl());
        descriptionField.setText(listing.getDescription());
    }

    @FXML
    private void onSave() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        String description = descriptionField.getText().trim();
        String ecoBadge = ecoBadgeField.getText().trim();
        String imageUrl = imageUrlField.getText().trim();
        Category category = categoryCombo.getValue();
        ProductCondition condition = conditionCombo.getValue();

        if (title.isBlank() || location.isBlank() || description.isBlank()) {
            fail("Title, location and description are required.");
            return;
        }
        if (category == null || condition == null) {
            fail("Choose a category and a condition.");
            return;
        }

        double price;
        double carbonSavedKg;
        int stockQuantity;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            carbonSavedKg = carbonField.getText().isBlank()
                    ? 0 : Double.parseDouble(carbonField.getText().trim());
            stockQuantity = Integer.parseInt(stockField.getText().trim());
        } catch (Exception e) {
            fail("Enter valid numeric price, stock and CO₂ values.");
            return;
        }
        if (price < 0 || carbonSavedKg < 0 || stockQuantity < 0) {
            fail("Price, stock and CO₂ saved cannot be negative.");
            return;
        }

        if (existing == null) {
            service.createListing(title, category, condition, price, location, description,
                    stockQuantity, ecoBadge.isBlank() ? null : ecoBadge,
                    carbonSavedKg, imageUrl.isBlank() ? null : imageUrl);
        } else {
            existing.setTitle(title);
            existing.setCategory(category);
            existing.setCondition(condition);
            existing.setPrice(price);
            existing.setStockQuantity(stockQuantity);
            existing.setCarbonSavedKg(carbonSavedKg);
            existing.setLocation(location);
            existing.setEcoBadge(ecoBadge.isBlank() ? null : ecoBadge);
            existing.setImageUrl(imageUrl.isBlank() ? null : imageUrl);
            existing.setDescription(description);
            service.updateListing(existing);
        }

        if (onSaved != null) onSaved.run();
        closeStage();
    }

    @FXML
    private void onCancel() {
        closeStage();
    }

    private void fail(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void closeStage() {
        Scene scene = saveButton.getScene();
        if (scene != null && scene.getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
