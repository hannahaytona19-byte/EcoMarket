package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.service.MockDataService;
import com.ecomarket.ui.BrandAssets;
import com.ecomarket.ui.LogoView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.regex.Pattern;

/** Registration form. Accounts are created only in memory. */
public class RegisterController {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private LogoView navbarLogo;
    @FXML private LogoView cardLogo;

    @FXML
    private void initialize() {
        navbarLogo.setLogoSize(BrandAssets.SIZE_HEADER);
        cardLogo.setLogoSize(BrandAssets.SIZE_LOGIN);
        firstNameField.requestFocus();
    }

    @FXML
    private void onEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onRegister();
            event.consume();
        }
    }

    @FXML
    private void onRegister() {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (first.isBlank() || last.isBlank()) {
            fail("Please enter your first and last name.");
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            fail("Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            fail("Password must be at least 6 characters.");
            return;
        }

        MockDataService.getInstance().register(first, last, email, password);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        SceneNavigator.navigate("browse");
    }

    @FXML
    private void onLogin() {
        SceneNavigator.navigate("login");
    }

    private void fail(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
