package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

/**
 * Single persistent layout for both auth screens. The backdrop, glass container
 * and back button are created once; switching login <-> register only moves the
 * two halves and cross-fades their content.
 */
public class AuthLayoutController {

    /**
     * Drop a leaf image at one of these paths to replace the CSS gradient.
     * Nothing else needs to change; the first resource found is used as a
     * full-screen background and the dark overlay above it keeps text readable.
     */
    private static final String[] BACKDROP_CANDIDATES = {
            "/img/auth-leaves.jpg", "/img/auth-leaves.png"
    };

    private static final String LOGIN_HEADING = "Welcome Back";
    private static final String LOGIN_COPY =
            "Pick up where you left off and keep giving good things a second life.";
    private static final String REGISTER_HEADING = "Let's Get Started";
    private static final String REGISTER_COPY =
            "Join EcoMarket to reuse, resell, and rehome pre-loved items with your neighbors.";

    private static final double COMPACT_WIDTH = 760;
    private static final Duration SWAP_DURATION = Duration.seconds(0.62);
    private static final Duration CROSS_FADE_AT = Duration.seconds(0.26);

    @FXML private StackPane authScreen;
    @FXML private Pane authCard;
    @FXML private StackPane infoPane;
    @FXML private StackPane formPane;
    @FXML private VBox infoContent;
    @FXML private Label infoHeading;
    @FXML private Label infoParagraph;
    @FXML private StackPane formContent;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private LoginController loginFormController;
    @FXML private RegisterController registerFormController;

    private String mode = "login";
    private boolean firstShow = true;
    private boolean compact;
    private Timeline swap;

    @FXML
    private void initialize() {
        installBackdrop();

        // Rounded clipping keeps the sliding halves inside the glass container.
        Rectangle clip = new Rectangle();
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        clip.widthProperty().bind(authCard.widthProperty());
        clip.heightProperty().bind(authCard.heightProperty());
        authCard.setClip(clip);

        authScreen.widthProperty().addListener((obs, oldValue, newValue) ->
                updateCompact(newValue.doubleValue()));
        authCard.widthProperty().addListener((obs, oldValue, newValue) -> layout());
        authCard.heightProperty().addListener((obs, oldValue, newValue) -> layout());

        updateCompact(authScreen.getWidth());
        applyModeInstant("register".equals(mode));
    }

    /**
     * Called by SceneNavigator for both routes. The very first call (direct link,
     * refresh or entering from another page) renders the correct layout instantly.
     */
    public void show(String route) {
        boolean targetRegister = "register".equals(route);

        // A role may be chosen in role-select immediately before login. When the
        // layout is reused the form controller is not re-created, so re-apply it.
        if (!firstShow && !targetRegister) {
            loginFormController.applyRoleContext(SceneNavigator.consumeContext());
        }

        boolean instant = firstShow || reducedMotion() || compact;
        firstShow = false;

        if (instant) {
            stopSwap();
            loginFormController.applyRoleContext(SceneNavigator.consumeContext());
            applyModeInstant(targetRegister);
        } else {
            animateTo(targetRegister);
        }
    }

    @FXML
    private void onBack() {
        SceneNavigator.navigate("landing");
    }

    // ------------------------------------------------------------------
    // Mode / layout
    // ------------------------------------------------------------------

    private void applyModeInstant(boolean register) {
        setMode(register);
        infoContent.setOpacity(1);
        formContent.setOpacity(1);
        applyPositions();
        focusActiveField();
    }

    private void setMode(boolean register) {
        mode = register ? "register" : "login";
        infoHeading.setText(register ? REGISTER_HEADING : LOGIN_HEADING);
        infoParagraph.setText(register ? REGISTER_COPY : LOGIN_COPY);

        loginForm.setVisible(!register);
        loginForm.setManaged(!register);
        registerForm.setVisible(register);
        registerForm.setManaged(register);

        loginFormController.setActive(!register);
        registerFormController.setActive(register);

        // Values in the two forms stay separate; passwords never carry over.
        loginFormController.clearSensitiveFields();
        registerFormController.clearSensitiveFields();
    }

    private void animateTo(boolean register) {
        stopSwap();

        double half = halfWidth();
        double infoTo = register ? 0 : half;
        double formTo = register ? 0 : -half;

        swap = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(infoPane.translateXProperty(), infoPane.getTranslateX(),
                                Interpolator.EASE_BOTH),
                        new KeyValue(formPane.translateXProperty(), formPane.getTranslateX(),
                                Interpolator.EASE_BOTH),
                        new KeyValue(infoContent.opacityProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(formContent.opacityProperty(), 1, Interpolator.EASE_BOTH)),

                new KeyFrame(CROSS_FADE_AT, event -> {
                    setMode(register);
                    infoContent.setOpacity(0);
                    formContent.setOpacity(0);
                },
                        new KeyValue(infoContent.opacityProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(formContent.opacityProperty(), 0, Interpolator.EASE_BOTH)),

                new KeyFrame(SWAP_DURATION, event -> {
                    infoContent.setOpacity(1);
                    formContent.setOpacity(1);
                    applyPositions();
                    focusActiveField();
                },
                        new KeyValue(infoPane.translateXProperty(), infoTo, Interpolator.EASE_BOTH),
                        new KeyValue(formPane.translateXProperty(), formTo, Interpolator.EASE_BOTH),
                        new KeyValue(infoContent.opacityProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(formContent.opacityProperty(), 1, Interpolator.EASE_BOTH))
        );
        swap.play();
    }

    private void stopSwap() {
        if (swap != null && swap.getStatus() == Animation.Status.RUNNING) {
            swap.stop();
        }
        swap = null;
    }

    private void updateCompact(double screenWidth) {
        boolean next = screenWidth < COMPACT_WIDTH;
        if (next == compact) {
            return;
        }
        compact = next;
        stopSwap();
        layout();
    }

    /** Manual layout lets the same two panes sit side-by-side or stacked. */
    private void layout() {
        double width = authCard.getWidth();
        double height = authCard.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        if (compact) {
            double infoHeight = Math.max(96, Math.min(150, height * 0.26));
            place(infoPane, 0, 0, width, infoHeight);
            place(formPane, 0, infoHeight, width, height - infoHeight);
            infoContent.setAlignment(Pos.CENTER);
            infoContent.setMaxWidth(width);
        } else {
            double half = halfWidth();
            place(infoPane, 0, 0, half, height);
            place(formPane, half, 0, width - half, height);
            infoContent.setAlignment(Pos.CENTER_LEFT);
            infoContent.setMaxWidth(StackPane.USE_COMPUTED_SIZE);
        }

        if (compact || swap == null || swap.getStatus() != Animation.Status.RUNNING) {
            applyPositions();
        }
    }

    private void place(StackPane pane, double x, double y, double width, double height) {
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.resize(width, height);
    }

    private void applyPositions() {
        if (compact) {
            infoPane.setTranslateX(0);
            formPane.setTranslateX(0);
            return;
        }
        boolean register = "register".equals(mode);
        double half = halfWidth();
        infoPane.setTranslateX(register ? 0 : half);
        formPane.setTranslateX(register ? 0 : -half);
    }

    private double halfWidth() {
        return authCard.getWidth() / 2;
    }

    private void focusActiveField() {
        Platform.runLater(() -> {
            if ("register".equals(mode)) {
                registerFormController.focusFirstField();
            } else {
                loginFormController.focusFirstField();
            }
        });
    }

    // ------------------------------------------------------------------
    // Backdrop / accessibility
    // ------------------------------------------------------------------

    private void installBackdrop() {
        for (String path : BACKDROP_CANDIDATES) {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                continue;
            }
            Image image = new Image(resource.toExternalForm());
            if (image.isError()) {
                continue;
            }
            BackgroundImage background = new BackgroundImage(image,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true));
            authScreen.setBackground(new Background(background));
            return;
        }
        // No file: the .auth-screen CSS gradient stays in place.
    }

    /**
     * JavaFX has no prefers-reduced-motion API. Support the usual desktop
     * equivalents: -Decomarket.reducedMotion=true or REDUCED_MOTION=1.
     */
    private static boolean reducedMotion() {
        return Boolean.getBoolean("ecomarket.reducedMotion")
                || "1".equals(System.getenv("REDUCED_MOTION"))
                || "true".equalsIgnoreCase(System.getenv("PREFERS_REDUCED_MOTION"));
    }
}
