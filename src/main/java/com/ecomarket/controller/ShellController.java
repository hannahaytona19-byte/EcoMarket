package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.service.MockDataService;
import com.ecomarket.ui.BrandAssets;
import com.ecomarket.ui.LogoView;
import com.ecomarket.util.UiUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistent application frame. The sidebar and top bar are never reloaded;
 * only the StackPane in the center swaps content when the route changes.
 */
public class ShellController {

    @FXML private LogoView sidebarLogo;
    @FXML private VBox navItems;
    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private Button notificationButton;
    @FXML private Button settingsButton;
    @FXML private Button aboutButton;
    @FXML private StackPane contentPane;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private String role = "BUYER";
    private String currentRoute = "browse";
    private Object currentController;

    @FXML
    private void initialize() {
        sidebarLogo.setLogoSize(BrandAssets.SIZE_HEADER);
        notificationButton.setTooltip(new Tooltip("Notifications"));
        settingsButton.setTooltip(new Tooltip("Profile & settings"));
        aboutButton.setTooltip(new Tooltip("About EcoMarket"));
    }

    /** Called by SceneNavigator whenever the shell needs a buyer or seller nav. */
    public void setRole(String role) {
        this.role = role;
        navItems.getChildren().clear();
        navButtons.clear();

        if ("SELLER".equals(role)) {
            addNav("seller", "📊", "Dashboard");
            addNav("seller", "📦", "My Listings");
            addNav("messages", "💬", "Messages");
            addNav("notifications", "🔔", "Notifications");
            addNav("profile", "👤", "Profile");
        } else {
            addNav("browse", "🔍", "Browse");
            addNav("notifications", "🔔", "Notifications");
            addNav("messages", "💬", "Messages");
            addNav("profile", "👤", "Profile");
        }
    }

    private void addNav(String route, String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("nav-icon");
        iconLabel.setPrefWidth(22);
        iconLabel.setAlignment(Pos.CENTER);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("nav-label");

        HBox graphic = new HBox(8, iconLabel, textLabel);
        graphic.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(graphic);
        button.getStyleClass().add("nav-item");
        button.setTooltip(new Tooltip(text));
        button.setOnAction(e -> {
            if ("seller".equals(route)) {
                SceneNavigator.navigate("seller");
            } else {
                SceneNavigator.navigate(route);
            }
        });
        navButtons.put(route + "#" + navButtons.size(), button);
        navItems.getChildren().add(button);
    }

    /** Swaps the center content for a logged-in route. */
    public void show(String route) {
        SceneNavigator.View view = SceneNavigator.loadView(route);
        Node content = view.root();
        contentPane.getChildren().setAll(content);
        currentController = view.controller();
        currentRoute = route;

        pageTitle.setText(titleFor(route));
        boolean isBrowse = "browse".equals(route);
        searchField.setVisible(isBrowse);
        searchField.setManaged(isBrowse);

        navButtons.values().forEach(b -> b.getStyleClass().remove("nav-item-active"));
        navButtons.forEach((key, button) -> {
            String buttonRoute = key.substring(0, key.indexOf('#'));
            if (buttonRoute.equals(route)) {
                button.getStyleClass().add("nav-item-active");
            }
        });

        // Any context not consumed by the just-created controller is stale.
        SceneNavigator.clearContext();
    }

    private String titleFor(String route) {
        return switch (route) {
            case "browse" -> "Browse";
            case "notifications" -> "Notifications";
            case "messages" -> "Messages";
            case "profile" -> "Profile";
            case "seller" -> "Seller Dashboard";
            default -> "EcoMarket";
        };
    }

    @FXML
    private void onSearchKey(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) return;
        String text = searchField.getText();
        if (currentController instanceof BrowseController browse) {
            browse.applySearch(text);
        } else {
            SceneNavigator.navigateWithContext("browse", text);
        }
        event.consume();
    }

    @FXML private void onNotifications() { SceneNavigator.navigate("notifications"); }

    @FXML private void onProfile() { SceneNavigator.navigate("profile"); }

    @FXML private void onAbout() { UiUtil.showAbout(); }

    @FXML
    private void onLogout() {
        MockDataService.getInstance().setCurrentUser(null);
        SceneNavigator.navigate("landing");
    }

    public String role() {
        return role;
    }
}
