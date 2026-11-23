package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
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
    @FXML private TextField passwordVisibleField;
    @FXML private Button passwordToggle;
    @FXML private Label errorLabel;
    @FXML private Label emailError;
    @FXML private Label passwordError;
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
        passwordVisibleField.clear();
        showPassword(false);
        clearErrors();
    }

    public void focusFirstField() {
        emailField.requestFocus();
    }

    @FXML
    private void onTogglePassword() {
        boolean revealing = passwordField.isVisible();
        if (revealing) {
            passwordVisibleField.setText(passwordField.getText());
        } else {
            passwordField.setText(passwordVisibleField.getText());
        }
        showPassword(!revealing);
        (revealing ? passwordVisibleField : passwordField).requestFocus();
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
        syncPassword();
        clearErrors();
        submitButton.setDisable(true);
        try {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (email.isBlank() || password.isBlank()) {
                if (email.isBlank()) {
                    setInlineError(emailError, "Please enter both your email and password.");
                }
                if (password.isBlank()) {
                    setInlineError(passwordError, "Please enter both your email and password.");
                }
                return;
            }
            if (!EMAIL.matcher(email.trim()).matches()) {
                setInlineError(emailError, "Please enter a valid email address.");
                return;
            }
            if (password.length() < 6) {
                setInlineError(passwordError, "Password must be at least 6 characters.");
                return;
            }

            Optional<User> user = MockDataService.getInstance().authenticate(email, password);
            if (user.isEmpty()) {
                UiUtil.showError("Incorrect email or password.");
                return;
            }

            // Role selection is required before login; the chosen role decides the landing screen.
            boolean seller = preferredRole == null
                    ? user.get().getRole() == User.Role.SELLER
                    : "SELLER".equals(preferredRole);
            SceneNavigator.navigate(seller ? "seller" : "browse");
        } finally {
            submitButton.setDisable(false);
        }
    }

    @FXML
    private void onRegister() {
        SceneNavigator.navigate("register");
    }

    private void syncPassword() {
        if (passwordVisibleField.isVisible()) {
            passwordField.setText(passwordVisibleField.getText());
        }
    }

    private void showPassword(boolean visible) {
        passwordField.setVisible(!visible);
        passwordField.setManaged(!visible);
        passwordVisibleField.setVisible(visible);
        passwordVisibleField.setManaged(visible);
        passwordToggle.setText(visible ? "Hide" : "Show");
    }

    private void setInlineError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        clearError();
        hideInline(emailError);
        hideInline(passwordError);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private static void hideInline(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
