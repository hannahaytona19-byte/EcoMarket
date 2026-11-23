package com.ecomarket.controller;

import com.ecomarket.model.Listing;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

/** Seller-only screen: summary cards plus listing management actions. */
public class SellerDashboardController {

    @FXML private Label activeCount;
    @FXML private Label soldCount;
    @FXML private Label unreadCount;
    @FXML private Label carbonCount;
    @FXML private VBox listingRows;
    @FXML private Button postButton;

    private final MockDataService service = MockDataService.getInstance();

    @FXML
    private void initialize() {
        postButton.setTooltip(new Tooltip("Create a new listing"));
        refresh();
    }

    private User seller() {
        return service.getCurrentUser() == null ? service.demoSeller() : service.getCurrentUser();
    }

    private void refresh() {
        var listings = service.getListingsBySeller(seller());
        long active = listings.stream().filter(l -> l.getStatus() == Listing.Status.ACTIVE).count();
        long sold = listings.stream().filter(l -> l.getStatus() == Listing.Status.SOLD).count();

        double carbon = listings.stream()
                .filter(l -> l.getStatus() == Listing.Status.ACTIVE)
                .mapToDouble(Listing::getCarbonSavedKg)
                .sum();

        activeCount.setText(String.valueOf(active));
        soldCount.setText(String.valueOf(sold));
        unreadCount.setText(String.valueOf(service.unreadNotificationCount()));
        carbonCount.setText(String.format(Locale.US, "%.2f", carbon));

        listingRows.getChildren().clear();
        if (listings.isEmpty()) {
            Label empty = new Label("You have no listings yet. Post your first item!");
            empty.getStyleClass().add("empty-state");
            listingRows.getChildren().add(empty);
            return;
        }
        listings.forEach(l -> listingRows.getChildren().add(row(l)));
    }

    private HBox row(Listing listing) {
        HBox row = new HBox(14);
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);

        String icon = listing.getCategory() == null ? "📦" : listing.getCategory().getIcon();
        StackPane thumb = UiUtil.placeholder(icon, listing.getTint(), 54);
        thumb.setPrefWidth(78);
        thumb.setMinWidth(78);
        row.getChildren().add(thumb);

        VBox info = new VBox(3);
        Label title = new Label(listing.getTitle());
        title.getStyleClass().add("card-title");
        String condition = listing.getCondition() == null ? "" : listing.getCondition().getLabel();
        String eco = UiUtil.ecoSummary(listing);
        Label meta = new Label(condition
                + (eco.isBlank() ? "" : "   •   " + eco)
                + "   •   " + UiUtil.currency(listing.getPrice()));
        meta.getStyleClass().add("listing-meta");
        info.getChildren().addAll(title, meta);
        HBox.setHgrow(info, Priority.ALWAYS);
        row.getChildren().add(info);

        Label status = new Label(listing.getStatus().name());
        status.getStyleClass().add(
                listing.getStatus() == Listing.Status.ACTIVE ? "status-active" : "status-sold");
        row.getChildren().add(status);

        Button edit = smallButton("Edit", "button-secondary");
        edit.setTooltip(new Tooltip("Edit this listing"));
        edit.setOnAction(e -> UiUtil.showListingForm(listing, this::refresh));

        Button markSold = smallButton("Mark as Sold", "button-secondary");
        markSold.setTooltip(new Tooltip("Mark this item as sold"));
        markSold.setDisable(listing.getStatus() == Listing.Status.SOLD);
        markSold.setOnAction(e -> {
            service.markSold(listing);
            refresh();
        });

        Button delete = smallButton("Delete", "button-danger");
        delete.setTooltip(new Tooltip("Delete this listing"));
        delete.setOnAction(e -> {
            if (UiUtil.confirm("Delete \"" + listing.getTitle() + "\"?")) {
                service.deleteListing(listing);
                refresh();
            }
        });

        row.getChildren().addAll(edit, markSold, delete);
        return row;
    }

    private Button smallButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClass, "button-small");
        return button;
    }

    @FXML
    private void onPost() {
        UiUtil.showListingForm(null, this::refresh);
    }
}
