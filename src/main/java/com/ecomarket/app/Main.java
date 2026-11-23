package com.ecomarket.app;

import com.ecomarket.util.UiUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the frontend-only EcoMarket prototype.
 * Run with: mvn javafx:run
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent landing = FXMLLoader.load(getClass().getResource("/view/landing.fxml"));

        Scene scene = new Scene(landing, 1280, 800);
        scene.getStylesheets().add(UiUtil.stylesheet());

        primaryStage.setTitle("EcoMarket — Reuse. Resell. Rehome.");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(680);
        primaryStage.setResizable(true);

        SceneNavigator.init(primaryStage, scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
