package io.ecomarket.desktop;

import io.ecomarket.desktop.config.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoMarketApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(EcoMarketApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            log.info("Starting ecoMarket Desktop Application...");
            
            // Try initializing Supabase pool asynchronously so UI stays responsive
            new Thread(DatabaseManager::initialize).start();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/ecomarket/desktop/ui/main-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(getClass().getResource("/io/ecomarket/desktop/ui/styles.css").toExternalForm());

            primaryStage.setTitle("ecoMarket — Sustainable Desktop Marketplace");
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            log.error("Failed to start ecoMarket application: ", e);
        }
    }

    @Override
    public void stop() {
        log.info("Shutting down ecoMarket...");
        DatabaseManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
