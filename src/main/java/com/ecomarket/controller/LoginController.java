package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
import com.ecomarket.ui.BrandAssets;
import com.ecomarket.ui.LogoView;
import javafx.fxml.FXML;
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
    @FXML private LogoView navbarLogo;
    @FXML private LogoView cardLogo;

    private String preferredRole;

    @FXML
    private void initialize() {
        navbarLogo.setLogoSize(BrandAssets.SIZE_HEADER);
        cardLogo.setLogoSize(BrandAssets.SIZE_LOGIN);

        Object context = SceneNavigator.consumeContext();
        if (context instanceof String role && ("BUYER".equals(role) || "SELLER".equals(role))) {
            preferredRole = role;
        }
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

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

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
}
