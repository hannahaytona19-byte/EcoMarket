package com.ecomarket.app;

import com.ecomarket.ui.BrandAssets;
import com.ecomarket.util.UiUtil;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Entry point for the frontend-only EcoMarket prototype.
 * Run with: mvn javafx:run
 */
public class Main extends Application {

    private static final Duration SPLASH_TIME = Duration.millis(700);

    @Override
    public void start(Stage primaryStage) {
        Stage splash = createSplash();
        splash.show();

        PauseTransition loading = new PauseTransition(SPLASH_TIME);
        loading.setOnFinished(event -> {
            try {
                Parent landing = FXMLLoader.load(getClass().getResource("/view/landing.fxml"));

                Scene scene = new Scene(landing, 1280, 800);
                scene.getStylesheets().add(UiUtil.stylesheet());

                primaryStage.setTitle(BrandAssets.APP_TITLE);
                primaryStage.getIcons().add(BrandAssets.windowIcon());
                primaryStage.setScene(scene);
                primaryStage.setMinWidth(1024);
                primaryStage.setMinHeight(680);
                primaryStage.setResizable(true);

                SceneNavigator.init(primaryStage, scene);
                primaryStage.show();
                splash.close();
            } catch (Exception failure) {
                splash.close();
                failure.printStackTrace();
                System.exit(1);
            }
        });
        loading.play();
    }

    private Stage createSplash() {
        ImageView logo = BrandAssets.imageView(BrandAssets.SIZE_SPLASH);

        Label name = new Label(BrandAssets.APP_NAME);
        name.getStyleClass().add("splash-title");
        Label tagline = new Label(BrandAssets.TAGLINE);
        tagline.getStyleClass().add("splash-tagline");

        VBox box = new VBox(14, logo, name, tagline);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: " + BrandAssets.CREAM
                + "; -fx-border-color: " + BrandAssets.DARK_GREEN
                + "; -fx-border-width: 3;");

        Scene scene = new Scene(box, 520, 430);
        scene.getStylesheets().add(UiUtil.stylesheet());

        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.setTitle(BrandAssets.APP_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
