package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Optional;
import java.util.regex.Pattern;

/** Login form with lightweight client-side validation. */
public class LoginController {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;

    private String preferredRole;

    @FXML
    private void initialize() {
        applyRoleContext(SceneNavigator.consumeContext());
    }

    /** Re-applies a role chosen in role-select when the shared layout is reused. */
    public void applyRoleContext(Object context) {
        if (context instanceof String role && ("BUYER".equals(role) || "SELLER".equals(role))) {
            preferredRole = role;
        }
    }

    /** Only the visible form owns the Scene's default button. */
    public void setActive(boolean active) {
        submitButton.setDefaultButton(active);
    }

    /** Passwords and errors never carry across a login/register switch. */
    public void clearSensitiveFields() {
        passwordField.clear();
        clearError();
    }

    public void focusFirstField() {
        emailField.requestFocus();
    }

    @FXML
    private void onEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLogin();
            event.consume();
        }
    }

    @FXML
    private void onLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            fail("Please enter both your email and password.");
            return;
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            fail("Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            fail("Password must be at least 6 characters.");
            return;
        }

        Optional<User> user = MockDataService.getInstance().authenticate(email, password);
        if (user.isEmpty()) {
            fail("Those credentials do not match a demo account.");
            return;
        }

        clearError();

        // Role selection is required before login; the chosen role decides the landing screen.
        boolean seller = preferredRole == null
                ? user.get().getRole() == User.Role.SELLER
                : "SELLER".equals(preferredRole);
        SceneNavigator.navigate(seller ? "seller" : "browse");
    }

    @FXML
    private void onRegister() {
        SceneNavigator.navigate("register");
    }

    private void fail(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
