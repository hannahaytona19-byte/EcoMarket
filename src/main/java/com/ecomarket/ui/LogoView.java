package com.ecomarket.ui;

import com.ecomarket.app.SceneNavigator;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

/**
 * The single reusable EcoMarket badge used on every screen.
 *
 * <p>The artwork comes from {@link BrandAssets}; this class only owns sizing,
 * high-DPI selection and the click-to-home behaviour. The badge is square, so
 * both {@link #setSize(double, double)} and {@link #setLogoSize(double)} use a
 * single uniform scale and preserve the aspect ratio.</p>
 */
public class LogoView extends StackPane {

    private final ImageView logo;
    private double logoSize;

    public LogoView() {
        logo = BrandAssets.imageView(BrandAssets.SIZE_HEADER);
        getChildren().add(logo);
        getStyleClass().add("eco-logo");
        setAlignment(Pos.CENTER);
        setCursor(Cursor.HAND);
        setFocusTraversable(true);
        setPickOnBounds(true);
        Tooltip.install(this, new Tooltip("Go to EcoMarket home"));

        setOnMouseClicked(event -> goHome());
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                goHome();
                event.consume();
            }
        });

        setLogoSize(BrandAssets.SIZE_HEADER);
    }

    /**
     * Backwards-compatible signature used by the existing controllers. The
     * larger of the two values is used as a square size so the badge is never
     * stretched to match the old sprout-plus-wordmark proportions.
     */
    public void setSize(double width, double height) {
        setLogoSize(Math.max(width, height));
    }

    public void setLogoSize(double size) {
        double square = Math.max(12, Math.ceil(size));
        this.logoSize = square;
        logo.setFitWidth(square);
        logo.setFitHeight(square);
        setPrefSize(square, square);
        setMinSize(square, square);
        setMaxSize(square, square);
    }

    public double getLogoSize() {
        return logoSize;
    }

    private void goHome() {
        SceneNavigator.navigate(SceneNavigator.homeRoute());
    }
}
