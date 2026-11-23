package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.service.MockDataService;
import com.ecomarket.ui.BrandAssets;
import com.ecomarket.ui.LogoView;
import javafx.fxml.FXML;

/** Role selection: a demo shortcut or the first step of the login flow. */
public class RoleSelectController {

    private static final String LOGIN_FLOW = "LOGIN";

    @FXML private LogoView roleLogo;

    private boolean loginFlow;

    @FXML
    private void initialize() {
        roleLogo.setLogoSize(BrandAssets.SIZE_ROLE);
        Object context = SceneNavigator.consumeContext();
        loginFlow = LOGIN_FLOW.equals(context);
    }

    @FXML
    private void onBuyer() {
        choose("BUYER");
    }

    @FXML
    private void onSeller() {
        choose("SELLER");
    }

    private void choose(String role) {
        if (loginFlow) {
            // Only after a role is chosen do we advance to the login form.
            SceneNavigator.navigateWithContext("login", role);
        } else if ("SELLER".equals(role)) {
            MockDataService.getInstance().enterAsSeller();
            SceneNavigator.navigate("seller");
        } else {
            MockDataService.getInstance().enterAsBuyer();
            SceneNavigator.navigate("browse");
        }
    }

    @FXML
    private void onBack() {
        SceneNavigator.navigate("landing");
    }
}
