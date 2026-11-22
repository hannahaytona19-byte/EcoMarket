package io.ecomarket.desktop.ui.dialog;

import io.ecomarket.desktop.config.DatabaseManager;
import io.ecomarket.desktop.config.SupabaseConfig;
import io.ecomarket.desktop.repository.UserRepository;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.UserRole;
import io.ecomarket.desktop.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class SettingsDialog extends Dialog<Void> {

    public SettingsDialog(UserRepository userRepo) {
        setTitle("Supabase Connection & Active User Session");
        setHeaderText("Manage Supabase database credentials and switch demo personas");
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(14);
        content.setPadding(new Insets(16));
        content.setPrefWidth(500);

        // Supabase Status
        GridPane dbGrid = new GridPane();
        dbGrid.setHgap(10);
        dbGrid.setVgap(8);

        Label statusVal = new Label(DatabaseManager.isAvailable() ? "CONNECTED (Live PostgreSQL)" : "OFFLINE / LOCAL DEMO MODE");
        statusVal.setStyle(DatabaseManager.isAvailable() ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;" : "-fx-text-fill: #757575; -fx-font-weight: bold;");

        dbGrid.add(new Label("Connection Status:"), 0, 0);
        dbGrid.add(statusVal, 1, 0);

        dbGrid.add(new Label("Supabase URL:"), 0, 1);
        dbGrid.add(new Label(SupabaseConfig.getSupabaseUrl()), 1, 1);

        dbGrid.add(new Label("DB Endpoint:"), 0, 2);
        dbGrid.add(new Label(SupabaseConfig.getDbUrl()), 1, 2);

        Button testConnBtn = new Button("Test Supabase Connection");
        testConnBtn.setOnAction(e -> {
            boolean ok = DatabaseManager.isAvailable();
            Alert a = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            a.setTitle("Connection Test");
            a.setContentText(ok ? "Supabase PostgreSQL is connected and responsive!" : "Could not connect to Supabase. Check credentials in supabase.properties or .env.");
            a.showAndWait();
        });

        // Demo User Switcher
        VBox userBox = new VBox(8);
        userBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 12; -fx-background-radius: 6;");

        Label userHeading = new Label("Quick Switch Persona / Role:");
        userHeading.setStyle("-fx-font-weight: bold;");

        ComboBox<String> personaCombo = new ComboBox<>();
        personaCombo.getItems().addAll(
                "buyer@ecomarket.io (BUYER - Eco Demo User)",
                "artisan@ecomarket.io (SELLER - Eco Artisan Studio)",
                "admin@ecomarket.io (ADMIN - Supervisor)"
        );

        User current = AuthService.getInstance().getCurrentUser();
        if (current != null && current.getRole() == UserRole.SELLER) {
            personaCombo.setValue("artisan@ecomarket.io (SELLER - Eco Artisan Studio)");
        } else if (current != null && current.getRole() == UserRole.ADMIN) {
            personaCombo.setValue("admin@ecomarket.io (ADMIN - Supervisor)");
        } else {
            personaCombo.setValue("buyer@ecomarket.io (BUYER - Eco Demo User)");
        }

        Button switchBtn = new Button("Switch Active Session");
        switchBtn.setOnAction(e -> {
            String selected = personaCombo.getValue();
            String email = selected.split(" ")[0];
            AuthService.getInstance().login(email, "password123");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Logged in as " + selected);
            alert.showAndWait();
        });

        userBox.getChildren().addAll(userHeading, personaCombo, switchBtn);
        content.getChildren().addAll(dbGrid, testConnBtn, new Separator(), userBox);
        getDialogPane().setContent(content);
    }
}
