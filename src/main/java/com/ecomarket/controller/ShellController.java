package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.service.MockDataService;
import com.ecomarket.ui.LogoView;
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
 * Persistent application frame. The sidebar and top bar are created once;
 * only the StackPane in the center swaps content when the route changes.
 */
public class ShellController {

    private record NavItem(String route, String icon, String label) {
    }

    /** Fixed seller sidebar: four items, in this order, on every seller page. */
    private static final NavItem[] SELLER_NAV_ITEMS = {
            new NavItem("seller", "📊", "Dashboard"),
            new NavItem("messages", "💬", "Messages"),
            new NavItem("notifications", "🔔", "Notifications"),
            new NavItem("profile", "👤", "Profile")
    };

    private static final NavItem[] BUYER_NAV_ITEMS = {
            new NavItem("browse", "🔍", "Browse"),
            new NavItem("messages", "💬", "Messages"),
            new NavItem("notifications", "🔔", "Notifications"),
            new NavItem("profile", "👤", "Profile")
    };

    @FXML private LogoView sidebarLogo;
    @FXML private VBox navItems;
    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private Button notificationButton;
    @FXML private Button settingsButton;
    @FXML private StackPane contentPane;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private String role = "BUYER";
    private String currentRoute = "browse";
    private Object currentController;
    private boolean navConfigured;

    @FXML
    private void initialize() {
        sidebarLogo.setSize(19, 26);
        notificationButton.setTooltip(new Tooltip("Notifications"));
        settingsButton.setTooltip(new Tooltip("Profile & settings"));
    }

    /**
     * Configures the sidebar once. SceneNavigator calls this only when the
     * shell is first created, so the nav set never changes between pages.
     */
    public void setRole(String role) {
        if (navConfigured) return;
        this.role = role;
        navItems.getChildren().clear();
        navButtons.clear();

        NavItem[] items = "SELLER".equals(role) ? SELLER_NAV_ITEMS : BUYER_NAV_ITEMS;
        for (NavItem item : items) {
            addNav(item);
        }
        navConfigured = true;
    }

    private void addNav(NavItem item) {
        Label iconLabel = new Label(item.icon());
        iconLabel.getStyleClass().add("nav-icon");
        iconLabel.setPrefWidth(22);
        iconLabel.setAlignment(Pos.CENTER_LEFT);

        Label textLabel = new Label(item.label());
        textLabel.getStyleClass().add("nav-label");

        HBox graphic = new HBox(10, iconLabel, textLabel);
        graphic.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(graphic);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-item");
        button.setTooltip(new Tooltip(item.label()));
        button.setOnAction(e -> SceneNavigator.navigate(item.route()));

        navButtons.put(item.route(), button);
        navItems.getChildren().add(button);
    }

    /** Swaps only the center content for a logged-in route. */
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

        navButtons.values().forEach(button -> button.getStyleClass().remove("nav-item-active"));
        Button active = navButtons.get(route);
        if (active != null) {
            active.getStyleClass().add("nav-item-active");
        }

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

    @FXML
    private void onLogout() {
        MockDataService.getInstance().setCurrentUser(null);
        SceneNavigator.navigate("landing");
    }

    public String role() {
        return role;
    }
}
