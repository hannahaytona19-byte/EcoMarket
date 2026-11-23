package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.Category;
import com.ecomarket.service.MockDataService;
import com.ecomarket.ui.BrandAssets;
import com.ecomarket.ui.LogoView;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Landing page: hero, category shortcuts, explainer cards and footer. */
public class LandingController {

    @FXML private ScrollPane landingScroll;
    @FXML private LogoView headerLogo;
    @FXML private LogoView footerLogo;
    @FXML private HBox heroColumns;
    @FXML private VBox heroCopy;
    @FXML private GridPane heroPhotos;
    @FXML private VBox categoriesSection;
    @FXML private VBox howSection;
    @FXML private FlowPane categoryFlow;

    private boolean stacked;

    @FXML
    private void initialize() {
        headerLogo.setLogoSize(BrandAssets.SIZE_HEADER);
        footerLogo.setLogoSize(BrandAssets.SIZE_FOOTER);

        for (Category category : MockDataService.getInstance().getCategories()) {
            Button chip = new Button(category.getIcon() + "  " + category.getName());
            chip.getStyleClass().add("chip");
            chip.setOnAction(e -> SceneNavigator.navigateWithContext("browse", category.getName()));
            categoryFlow.getChildren().add(chip);
        }

        landingScroll.widthProperty().addListener(
                (obs, oldWidth, newWidth) -> updateHeroLayout(newWidth.doubleValue()));
        updateHeroLayout(landingScroll.getWidth());
    }

    /** Stacks the hero copy above the photo grid on narrow windows. */
    private void updateHeroLayout(double width) {
        boolean shouldStack = width > 0 && width < 900;
        if (shouldStack == stacked) return;
        stacked = shouldStack;

        if (stacked) {
            VBox column = new VBox(24, heroCopy, heroPhotos);
            column.setAlignment(Pos.CENTER_LEFT);
            heroColumns.setAlignment(Pos.CENTER);
            heroColumns.getChildren().setAll(column);
        } else {
            heroColumns.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(heroCopy, Priority.ALWAYS);
            heroColumns.getChildren().setAll(heroCopy, heroPhotos);
        }
    }

    @FXML private void onBrowse() { SceneNavigator.navigate("role"); }

    @FXML private void onPost() { SceneNavigator.navigate("role"); }

    /** Login is gated by role selection; Start Browsing/Post keep the demo path. */
    @FXML private void onLogin() {
        SceneNavigator.navigateWithContext("role", "LOGIN");
    }

    @FXML private void onRegister() { SceneNavigator.navigate("register"); }

    @FXML private void onCategories() { scrollTo(categoriesSection); }

    @FXML private void onHowItWorks() { scrollTo(howSection); }

    private void scrollTo(Node target) {
        double contentHeight = landingScroll.getContent().getLayoutBounds().getHeight();
        double viewport = landingScroll.getHeight();
        double max = Math.max(1, contentHeight - viewport);
        landingScroll.setVvalue(Math.max(0, Math.min(1, target.getBoundsInParent().getMinY() / max)));
    }
}
