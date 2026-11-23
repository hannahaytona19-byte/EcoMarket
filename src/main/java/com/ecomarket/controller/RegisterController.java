package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML private TextField passwordVisibleField;
    @FXML private Button passwordToggle;
    @FXML private Label errorLabel;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Button submitButton;

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
        firstNameField.requestFocus();
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
            onRegister();
            event.consume();
        }
    }

    @FXML
    private void onRegister() {
        syncPassword();
        clearErrors();
        submitButton.setDisable(true);
        try {
            String first = firstNameField.getText().trim();
            String last = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (first.isBlank() || last.isBlank()) {
                if (first.isBlank()) {
                    setInlineError(firstNameError, "Please enter your first and last name.");
                }
                if (last.isBlank()) {
                    setInlineError(lastNameError, "Please enter your first and last name.");
                }
                return;
            }
            if (!EMAIL.matcher(email).matches()) {
                setInlineError(emailError, "Please enter a valid email address.");
                return;
            }
            if (password.length() < 6) {
                setInlineError(passwordError, "Password must be at least 6 characters.");
                return;
            }

            MockDataService.getInstance().register(first, last, email, password);
            clearError();
            UiUtil.showInfo("Account created! You can log in now.");
            SceneNavigator.navigate("login");
        } finally {
            submitButton.setDisable(false);
        }
    }

    @FXML
    private void onLogin() {
        SceneNavigator.navigate("login");
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
        hideInline(firstNameError);
        hideInline(lastNameError);
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
