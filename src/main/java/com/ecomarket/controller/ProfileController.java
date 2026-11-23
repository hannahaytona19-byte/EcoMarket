package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** User profile with My Listings / Saved Items tabs. */
public class ProfileController {

    @FXML private Label profileAvatar;
    @FXML private Label profileName;
    @FXML private Label profileEmail;
    @FXML private Label profileLocation;
    @FXML private Label profileSince;
    @FXML private Label profileEco;
    @FXML private Button editButton;
    @FXML private FlowPane myListingsFlow;
    @FXML private FlowPane savedFlow;

    private final MockDataService service = MockDataService.getInstance();
    private User user;

    @FXML
    private void initialize() {
        user = service.getCurrentUser() == null ? service.demoBuyer() : service.getCurrentUser();
        editButton.setTooltip(new Tooltip("Edit your name and location"));
        populateHeader();
        refresh();
    }

    private void populateHeader() {
        profileAvatar.setText(user.initials());
        profileAvatar.setAlignment(Pos.CENTER);
        profileAvatar.setPrefSize(72, 72);
        profileAvatar.setMinSize(72, 72);
        profileAvatar.setStyle("-fx-font-size: 24px;");
        profileName.setText(user.fullName());
        profileEmail.setText(user.getEmail());
        profileLocation.setText("📍 " + (user.getLocation() == null ? "—" : user.getLocation()));
        profileSince.setText("Member since " + user.getMemberSince()
                + (user.getAddress() == null || user.getAddress().isBlank()
                    ? "" : "   •   " + user.getAddress()));
        profileEco.setText("⚡ " + user.getEcoPoints() + " eco points"
                + (user.isVerified() ? "   •   Verified" : "   •   Unverified"));
    }

    private void refresh() {
        myListingsFlow.getChildren().clear();
        service.getListingsBySeller(user).forEach(l ->
                myListingsFlow.getChildren().add(UiUtil.listingCard(l, UiUtil::showListingDetails)));

        savedFlow.getChildren().clear();
        service.getSavedListings().forEach(l ->
                savedFlow.getChildren().add(UiUtil.listingCard(l, UiUtil::showListingDetails)));
    }

    @FXML
    private void onEdit() {
        TextField first = new TextField(user.getFirstName());
        TextField last = new TextField(user.getLastName());
        TextField location = new TextField(user.getLocation() == null ? "" : user.getLocation());
        first.setPromptText("First name");
        last.setPromptText("Last name");
        location.setPromptText("Location");

        VBox body = new VBox(10,
                labelled("First Name", first),
                labelled("Last Name", last),
                labelled("Location", location));
        body.getStyleClass().add("form-card");

        Button save = new Button("Save");
        save.getStyleClass().add("button-primary");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("button-secondary");
        javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(10, save, cancel);
        body.getChildren().add(actions);

        Stage dialog = new Stage();
        dialog.setTitle("EcoMarket — Edit Profile");
        dialog.initModality(Modality.WINDOW_MODAL);
        if (SceneNavigator.stage() != null) dialog.initOwner(SceneNavigator.stage());
        Scene scene = new Scene(body, 400, 340);
        scene.getStylesheets().add(UiUtil.stylesheet());
        dialog.setScene(scene);
        dialog.setResizable(false);

        save.setOnAction(e -> {
            if (first.getText().isBlank() || last.getText().isBlank()) {
                UiUtil.showError("First and last name are required.");
                return;
            }
            user.setFirstName(first.getText().trim());
            user.setLastName(last.getText().trim());
            user.setLocation(location.getText().trim());
            populateHeader();
            refresh();
            dialog.close();
        });
        cancel.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private VBox labelled(String text, TextField field) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return new VBox(4, label, field);
    }
}
