package io.ecomarket.desktop.ui.dialog;

import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.UserRole;
import io.ecomarket.desktop.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AuthDialog extends Dialog<User> {

    public AuthDialog() {
        setTitle("ecoMarket — Account Authentication");
        setHeaderText("Sign in or create a sustainable commerce account");

        ButtonType actionBtnType = new ButtonType("Sign In", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(actionBtnType, ButtonType.CANCEL);

        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setPrefWidth(420);

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton signInRadio = new RadioButton("Sign In to Existing Account");
        signInRadio.setToggleGroup(modeGroup);
        signInRadio.setSelected(true);

        RadioButton registerRadio = new RadioButton("Create New Sustainable Account");
        registerRadio.setToggleGroup(modeGroup);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Your Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("user@example.com");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        ComboBox<UserRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().setAll(UserRole.BUYER, UserRole.SELLER);
        roleCombo.setValue(UserRole.BUYER);

        Label nameLabel = new Label("Full Name:");
        Label roleLabel = new Label("Account Type:");

        form.add(emailField, 1, 0);
        form.add(new Label("Email:"), 0, 0);
        form.add(passwordField, 1, 1);
        form.add(new Label("Password:"), 0, 1);

        // Name and Role only for registration
        form.add(nameLabel, 0, 2);
        form.add(nameField, 1, 2);
        form.add(roleLabel, 0, 3);
        form.add(roleCombo, 1, 3);

        nameLabel.setVisible(false);
        nameField.setVisible(false);
        roleLabel.setVisible(false);
        roleCombo.setVisible(false);

        modeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isRegister = newT == registerRadio;
            nameLabel.setVisible(isRegister);
            nameField.setVisible(isRegister);
            roleLabel.setVisible(isRegister);
            roleCombo.setVisible(isRegister);
            Button btn = (Button) getDialogPane().lookupButton(actionBtnType);
            if (btn != null) {
                btn.setText(isRegister ? "Create Account" : "Sign In");
            }
        });

        container.getChildren().addAll(signInRadio, registerRadio, new Separator(), form);
        getDialogPane().setContent(container);

        setResultConverter(dialogButton -> {
            if (dialogButton == actionBtnType) {
                String email = emailField.getText().trim();
                String pass = passwordField.getText().trim();

                if (email.isBlank() || pass.isBlank()) {
                    showAlert("Please enter both email and password.");
                    return null;
                }

                if (registerRadio.isSelected()) {
                    String name = nameField.getText().trim();
                    if (name.isBlank()) {
                        showAlert("Please provide your full name for registration.");
                        return null;
                    }
                    try {
                        return AuthService.getInstance().register(name, email, pass, roleCombo.getValue());
                    } catch (Exception ex) {
                        showAlert("Registration error: " + ex.getMessage());
                        return null;
                    }
                } else {
                    boolean ok = AuthService.getInstance().login(email, pass);
                    if (ok) {
                        return AuthService.getInstance().getCurrentUser();
                    } else {
                        showAlert("Invalid credentials or user not found.");
                        return null;
                    }
                }
            }
            return null;
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
