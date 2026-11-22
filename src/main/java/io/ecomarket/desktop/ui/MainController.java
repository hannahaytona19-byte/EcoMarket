package io.ecomarket.desktop.ui;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.config.SupabaseConfig;
import io.ecomarket.desktop.repository.CategoryRepository;
import io.ecomarket.desktop.repository.OrderRepository;
import io.ecomarket.desktop.repository.ProductRepository;
import io.ecomarket.desktop.repository.ReviewRepository;
import io.ecomarket.desktop.repository.UserRepository;
import io.ecomarket.desktop.schema.dto.CheckoutRequest;
import io.ecomarket.desktop.schema.dto.EcoImpactSummary;
import io.ecomarket.desktop.schema.entity.CartItem;
import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.DeliveryMethod;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import io.ecomarket.desktop.schema.enums.UserRole;
import io.ecomarket.desktop.service.AuthService;
import io.ecomarket.desktop.service.CartService;
import io.ecomarket.desktop.service.OrderService;
import io.ecomarket.desktop.ui.dialog.AddProductDialog;
import io.ecomarket.desktop.ui.dialog.AuthDialog;
import io.ecomarket.desktop.ui.dialog.OrdersDialog;
import io.ecomarket.desktop.ui.dialog.ProductDetailDialog;
import io.ecomarket.desktop.ui.dialog.SettingsDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.List;

public class MainController {

    @FXML private Label connectionStatusLabel;
    @FXML private Label userEcoPointsLabel;
    @FXML private Label userNameLabel;
    @FXML private Button listProductBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> conditionFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private FlowPane productsGrid;

    // Cart pane components
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartProductCol;
    @FXML private TableColumn<CartItem, String> cartQtyCol;
    @FXML private TableColumn<CartItem, String> cartPriceCol;
    @FXML private Label cartTotalLabel;
    @FXML private Label carbonSavedLabel;
    @FXML private Label pointsEarnedLabel;
    @FXML private ComboBox<DeliveryMethod> deliveryMethodCombo;
    @FXML private TextField addressField;
    @FXML private Button checkoutButton;

    private final ProductRepository productRepo = new ProductRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final UserRepository userRepo = new UserRepository();
    private final OrderRepository orderRepo = new OrderRepository();
    private final ReviewRepository reviewRepo = new ReviewRepository();
    private final OrderService orderService = new OrderService();
    private CartService cartService;

    @FXML
    public void initialize() {
        AuthService auth = AuthService.getInstance();
        User currentUser = auth.getCurrentUser();
        cartService = new CartService(currentUser);

        auth.currentUserProperty().addListener((obs, oldU, newU) -> {
            if (newU != null) {
                updateUserBadge();
                if (addressField != null && newU.getAddress() != null) {
                    addressField.setText(newU.getAddress());
                }
            }
        });

        // Populate filter combos
        conditionFilterCombo.getItems().addAll("ALL CONDITIONS", "NEW", "UPCYCLED", "REFURBISHED", "PRELOVED");
        conditionFilterCombo.setValue("ALL CONDITIONS");

        categoryFilterCombo.getItems().add("ALL CATEGORIES");
        for (Category cat : categoryRepo.findAll()) {
            categoryFilterCombo.getItems().add(cat.getName());
        }
        categoryFilterCombo.setValue("ALL CATEGORIES");

        deliveryMethodCombo.getItems().setAll(DeliveryMethod.values());
        deliveryMethodCombo.setValue(DeliveryMethod.GREEN_COURIER);

        if (addressField != null && currentUser.getAddress() != null) {
            addressField.setText(currentUser.getAddress());
        }

        setupCartTable();
        updateUserBadge();
        updateConnectionStatus();

        // Listeners for dynamic search & filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadProducts());
        conditionFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadProducts());
        categoryFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadProducts());

        // Initial products load
        loadProducts();
    }

    private void updateConnectionStatus() {
        new Thread(() -> {
            boolean available = DatabaseManager.isAvailable();
            Platform.runLater(() -> {
                if (available) {
                    connectionStatusLabel.setText("● Supabase Connected");
                    connectionStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                } else if (SupabaseConfig.isDbConfigured()) {
                    connectionStatusLabel.setText("○ Connecting to Supabase...");
                    connectionStatusLabel.setStyle("-fx-text-fill: #e65100;");
                } else {
                    connectionStatusLabel.setText("○ Supabase: Offline (Local Demo)");
                    connectionStatusLabel.setStyle("-fx-text-fill: #757575;");
                }
            });
        }).start();
    }

    private void setupCartTable() {
        cartProductCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProduct().getTitle()));
        cartQtyCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getQuantity())));
        cartPriceCol.setCellValueFactory(cell -> new SimpleStringProperty("$" + cell.getValue().getProduct().getPrice().multiply(BigDecimal.valueOf(cell.getValue().getQuantity())).toString()));

        cartTable.setItems(cartService.getCartItems());
        cartService.getCartItems().addListener((javafx.collections.ListChangeListener<CartItem>) c -> updateCartSummary());
    }

    private void updateUserBadge() {
        User u = AuthService.getInstance().getCurrentUser();
        if (u != null) {
            if (userNameLabel != null) userNameLabel.setText(u.getFullName() + " (" + u.getRole() + ")");
            userEcoPointsLabel.setText(u.getEcoPoints() + " Eco-Points");
            if (listProductBtn != null) {
                listProductBtn.setVisible(true);
            }
        }
    }

    private void updateCartSummary() {
        EcoImpactSummary summary = cartService.getImpactSummary();
        cartTotalLabel.setText(String.format("$%.2f", summary.orderTotal()));
        carbonSavedLabel.setText(String.format("%.2f kg CO2", summary.totalCarbonSavedKg()));
        pointsEarnedLabel.setText("+" + summary.ecoPointsEarned() + " pts");
        checkoutButton.setDisable(cartService.getCartItems().isEmpty());
    }

    public void loadProducts() {
        String keyword = searchField.getText();
        String condStr = conditionFilterCombo.getValue();
        ProductCondition cond = (condStr == null || "ALL CONDITIONS".equals(condStr)) ? null : ProductCondition.valueOf(condStr);

        String catStr = categoryFilterCombo.getValue();
        Integer catId = null;
        if (catStr != null && !"ALL CATEGORIES".equals(catStr)) {
            for (Category c : categoryRepo.findAll()) {
                if (c.getName().equalsIgnoreCase(catStr)) {
                    catId = c.getCategoryId();
                    break;
                }
            }
        }

        List<Product> products = productRepo.filter(keyword, catId, cond);
        productsGrid.getChildren().clear();

        for (Product product : products) {
            productsGrid.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(240);
        card.setPadding(new Insets(12));

        // Eco Badge tag
        HBox tagBar = new HBox(6);
        Label badgeLabel = new Label(p.getEcoBadge() != null ? p.getEcoBadge() : p.getCondition().name());
        badgeLabel.getStyleClass().add("badge-eco");

        Label carbonLabel = new Label("-" + p.getCarbonSavedKg() + " kg CO2");
        carbonLabel.getStyleClass().add("badge-carbon");
        tagBar.getChildren().addAll(badgeLabel, carbonLabel);

        Label titleLabel = new Label(p.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("product-title");

        Label descLabel = new Label(p.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);
        descLabel.getStyleClass().add("product-desc");

        // Make card clickable to open detail dialog
        card.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof Button) return;
            ProductDetailDialog dialog = new ProductDetailDialog(p, reviewRepo);
            dialog.showAndWait();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("$%.2f", p.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        Button addBtn = new Button("Add to Cart");
        addBtn.getStyleClass().add("btn-add-cart");
        addBtn.setOnAction(e -> {
            cartService.addProduct(p, 1);
        });

        footer.getChildren().addAll(priceLabel, addBtn);
        HBox.setHgrow(priceLabel, Priority.ALWAYS);

        card.getChildren().addAll(tagBar, titleLabel, descLabel, spacer, footer);
        return card;
    }

    @FXML
    private void handleOpenAddProduct() {
        AddProductDialog dialog = new AddProductDialog(categoryRepo, productRepo);
        dialog.showAndWait().ifPresent(p -> {
            loadProducts();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product \"" + p.getTitle() + "\" published to ecoMarket!");
        });
    }

    @FXML
    private void handleOpenOrders() {
        User u = AuthService.getInstance().getCurrentUser();
        OrdersDialog dialog = new OrdersDialog(u, orderRepo);
        dialog.showAndWait();
    }

    @FXML
    private void handleOpenAuth() {
        AuthDialog dialog = new AuthDialog();
        dialog.showAndWait().ifPresent(user -> {
            updateUserBadge();
            if (addressField != null && user.getAddress() != null) {
                addressField.setText(user.getAddress());
            }
            showAlert(Alert.AlertType.INFORMATION, "Welcome", "Signed in as " + user.getFullName() + " (" + user.getRole() + ")");
        });
    }

    @FXML
    private void handleOpenSettings() {
        SettingsDialog dialog = new SettingsDialog(userRepo);
        dialog.showAndWait();
        updateConnectionStatus();
        updateUserBadge();
    }

    @FXML
    private void handleRemoveSelectedCartItem() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartService.removeProduct(selected.getProduct().getProductId());
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartService.getCartItems().isEmpty()) {
            return;
        }

        String address = addressField.getText();
        if (address == null || address.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Checkout", "Please provide a shipping address.");
            return;
        }

        User u = AuthService.getInstance().getCurrentUser();
        DeliveryMethod method = deliveryMethodCombo.getValue();
        CheckoutRequest request = new CheckoutRequest(u.getUserId(), method, address);
        EcoImpactSummary impact = cartService.getImpactSummary();

        try {
            Order order = orderService.checkout(u, cartService.getCartItems(), request, impact);
            orderRepo.recordOrder(order);
            cartService.clear();
            updateUserBadge();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Confirmed!");
            alert.setHeaderText("Thank you for shopping sustainably!");
            alert.setContentText(String.format(
                    "Order Number: %s\nTotal Paid: $%.2f\nCarbon Footprint Offset: %.2f kg CO2\nEco-Points Awarded: +%d points\nDelivery: %s",
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    order.getTotalCarbonSavedKg(),
                    impact.ecoPointsEarned(),
                    order.getDeliveryMethod()
            ));
            alert.showAndWait();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Checkout Error", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
