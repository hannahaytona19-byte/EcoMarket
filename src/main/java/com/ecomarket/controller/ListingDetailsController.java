package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.Conversation;
import com.ecomarket.model.Listing;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Locale;

/** Read-only listing view. The only transaction action is "Message Seller". */
public class ListingDetailsController {

    @FXML private StackPane heroImage;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label conditionChip;
    @FXML private Label locationChip;
    @FXML private Node ecoInfo;
    @FXML private Label ecoBadgeChip;
    @FXML private Label carbonLabel;
    @FXML private Label stockLabel;
    @FXML private Label postedLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label sellerAvatar;
    @FXML private Label sellerName;
    @FXML private Label sellerSince;
    @FXML private Button saveButton;

    private Listing listing;

    public void setListing(Listing listing) {
        this.listing = listing;
        if (listing == null) return;

        String caption = listing.getCategory() == null
                ? "Item photo" : listing.getCategory().getIcon() + "  Item photo";
        heroImage.getChildren().setAll(UiUtil.placeholder(caption, listing.getTint(), 190));

        titleLabel.setText(listing.getTitle());
        priceLabel.setText(UiUtil.currency(listing.getPrice()));
        conditionChip.setText(listing.getCondition() == null
                ? "—" : listing.getCondition().getLabel());
        locationChip.setText(listing.getLocation());

        String ecoBadge = listing.getEcoBadge();
        ecoBadgeChip.setText(ecoBadge == null || ecoBadge.isBlank() ? "Eco pick" : ecoBadge);
        carbonLabel.setText(String.format(Locale.US, "%.2f kg CO₂ saved", listing.getCarbonSavedKg()));
        stockLabel.setText(listing.getStockQuantity() + " in stock");
        boolean showEco = listing.getCarbonSavedKg() > 0
                || (ecoBadge != null && !ecoBadge.isBlank())
                || listing.getStockQuantity() > 0;
        ecoInfo.setVisible(showEco);
        ecoInfo.setManaged(showEco);

        postedLabel.setText("Created " + listing.getCreatedAt()
                + "   •   Posted " + listing.getPostedAt()
                + (listing.getStatus() == Listing.Status.SOLD ? "   •   SOLD" : ""));
        descriptionLabel.setText(listing.getDescription());

        var seller = listing.getSeller();
        sellerAvatar.setText(seller == null ? "?" : seller.initials());
        sellerAvatar.setAlignment(Pos.CENTER);
        sellerAvatar.setPrefSize(44, 44);
        sellerAvatar.setMinSize(44, 44);
        sellerAvatar.setStyle("-fx-font-size: 15px;");
        sellerName.setText(seller == null ? "Unknown seller" : seller.fullName());
        sellerSince.setText(seller == null ? "" : "Member since " + seller.getMemberSince());
        updateSaveButton();

        saveButton.setTooltip(new Tooltip("Save this item to your profile"));
    }

    @FXML
    private void onSave() {
        if (listing == null) return;
        MockDataService.getInstance().toggleSaved(listing);
        updateSaveButton();
    }

    private void updateSaveButton() {
        boolean saved = listing != null && listing.isSaved();
        saveButton.setText(saved ? "♥ Saved" : "♡ Save");
    }

    @FXML
    private void onShare() {
        if (listing == null) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(listing.getTitle() + " — " + UiUtil.currency(listing.getPrice())
                + " on EcoMarket (" + listing.getLocation() + ")");
        Clipboard.getSystemClipboard().setContent(content);
        UiUtil.showInfo("Listing details copied to clipboard.");
    }

    @FXML
    private void onMessage() {
        Conversation conversation = MockDataService.getInstance().getOrCreateConversation(listing);
        closeStage();
        SceneNavigator.navigateWithContext("messages", conversation);
    }

    @FXML
    private void onClose() {
        closeStage();
    }

    private void closeStage() {
        Scene scene = saveButton.getScene();
        if (scene != null && scene.getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
