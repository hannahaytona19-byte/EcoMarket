package com.ecomarket.util;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.controller.ListingDetailsController;
import com.ecomarket.controller.ListingFormController;
import com.ecomarket.model.Listing;
import com.ecomarket.model.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

/** Small presentation helpers shared by every controller. */
public final class UiUtil {

    private static final String STYLESHEET = "/css/ecomarket.css";

    private UiUtil() {
    }

    /** Mirrors schema.sql's NUMERIC(10,2) prices without losing cents. */
    public static String currency(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "Php " + nf.format(amount);
    }

    /** One-line eco summary used on cards and dashboard rows. */
    public static String ecoSummary(Listing listing) {
        if (listing == null) return "";
        StringBuilder text = new StringBuilder();
        if (listing.getEcoBadge() != null && !listing.getEcoBadge().isBlank()) {
            text.append(listing.getEcoBadge());
        }
        if (listing.getCarbonSavedKg() > 0) {
            if (text.length() > 0) text.append("   •   ");
            text.append(String.format(Locale.US, "%.2f kg CO₂ saved", listing.getCarbonSavedKg()));
        }
        return text.toString();
    }

    public static Label avatar(User user, double size) {
        Label label = new Label(user == null ? "?" : user.initials());
        label.getStyleClass().add("avatar");
        label.setAlignment(Pos.CENTER);
        label.setPrefSize(size, size);
        label.setMinSize(size, size);
        label.setStyle("-fx-font-size: " + (size * 0.34) + "px;");
        return label;
    }

    /** Gray/warm-tinted image stand-in. No image files are used by the prototype. */
    public static StackPane placeholder(String caption, String tint, double height) {
        Label label = new Label(caption == null ? "Photo" : caption);
        label.getStyleClass().add("placeholder-caption");
        StackPane pane = new StackPane(label);
        pane.getStyleClass().add("image-placeholder");
        pane.setPrefHeight(height);
        pane.setMinHeight(height);
        if (tint != null && !tint.isBlank()) {
            pane.setStyle("-fx-background-color: " + tint + ";");
        }
        return pane;
    }

    /** Creates a focusable, keyboard-openable listing card. */
    public static VBox listingCard(Listing listing, Consumer<Listing> onOpen) {
        VBox card = new VBox(6);
        card.getStyleClass().add("listing-card");
        card.setPrefWidth(244);
        card.setFocusTraversable(true);

        StackPane image = placeholder(listing.getCategory() == null
                ? "Item photo" : listing.getCategory().getIcon() + "  Item photo",
                listing.getTint(), 132);

        Label title = new Label(listing.getTitle());
        title.getStyleClass().add("listing-title");
        title.setWrapText(true);

        Label price = new Label(currency(listing.getPrice()));
        price.getStyleClass().add("listing-price");

        String condition = listing.getCondition() == null ? "" : listing.getCondition().getLabel();
        String eco = ecoSummary(listing);
        Label meta = new Label(condition
                + (eco.isBlank() ? "" : "   •   " + eco)
                + "   •   " + listing.getLocation());
        meta.getStyleClass().add("listing-meta");

        if (listing.getStatus() == Listing.Status.SOLD) {
            Label sold = new Label("SOLD");
            sold.getStyleClass().add("status-sold");
            card.getChildren().add(0, sold);
        }

        card.getChildren().addAll(image, title, price, meta);
        Tooltip.install(card, new Tooltip("Open details for " + listing.getTitle()));
        card.setOnMouseClicked(e -> onOpen.accept(listing));
        card.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                onOpen.accept(listing);
                e.consume();
            }
        });
        return card;
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.CANCEL, ButtonType.OK);
        styleDialog(alert);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    /** One title and one dark theme for every app dialog. */
    private static void styleDialog(Dialog<?> dialog) {
        dialog.setTitle("EcoMarket");
        dialog.setHeaderText(null);
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("eco-dialog");
        pane.getStylesheets().add(STYLESHEET);
    }

    public static void showListingDetails(Listing listing) {
        openModal("listing-details.fxml", "Listing Details", 600, 620, controller -> {
            ((ListingDetailsController) controller).setListing(listing);
        }, true);
    }

    public static void showListingForm(Listing existing, Runnable onSaved) {
        openModal("listing-form.fxml",
                existing == null ? "Post New Item" : "Edit Listing", 720, 760, controller -> {
                    ((ListingFormController) controller).setListing(existing, onSaved);
                }, false);
    }

    private static void openModal(String fxml, String title, int width, int height,
                                  java.util.function.Consumer<Object> configurator,
                                  boolean capHeightToScreen) {
        try {
            FXMLLoader loader = new FXMLLoader(UiUtil.class.getResource("/view/" + fxml));
            Parent root = loader.load();
            configurator.accept(loader.getController());

            double sceneHeight = capHeightToScreen
                    ? Math.min(height, Screen.getPrimary().getVisualBounds().getHeight() * 0.85)
                    : height;
            Scene modalScene = new Scene(root, width, sceneHeight);
            modalScene.getStylesheets().add(STYLESHEET);

            Stage stage = new Stage();
            stage.setTitle("EcoMarket — " + title);
            stage.setScene(modalScene);
            stage.setResizable(false);
            if (SceneNavigator.stage() != null) {
                stage.initOwner(SceneNavigator.stage());
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.show();
        } catch (IOException e) {
            showError("Unable to open " + title + ": " + e.getMessage());
        }
    }

    public static String stylesheet() {
        return STYLESHEET;
    }
}
