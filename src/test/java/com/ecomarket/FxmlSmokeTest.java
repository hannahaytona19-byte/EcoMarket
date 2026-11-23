package com.ecomarket;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads every FXML file once on the JavaFX thread to verify controllers,
 * fx:ids and FXML imports are wired correctly.
 */
class FxmlSmokeTest {

    private static final List<String> VIEWS = List.of(
            "landing.fxml", "role-select.fxml", "login.fxml", "register.fxml",
            "shell.fxml", "browse.fxml", "notifications.fxml", "messages.fxml",
            "profile.fxml", "seller-dashboard.fxml",
            "listing-details.fxml", "listing-form.fxml"
    );

    @Test
    void allViewsLoad() throws Exception {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // Toolkit already running in this JVM.
        }

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                for (String view : VIEWS) {
                    new FXMLLoader(FxmlSmokeTest.class.getResource("/view/" + view)).load();
                }
            } catch (Throwable t) {
                java.io.StringWriter sw = new java.io.StringWriter();
                t.printStackTrace(new java.io.PrintWriter(sw));
                failure.set(new AssertionError(sw.toString()));
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(20, TimeUnit.SECONDS), "FXML loading timed out");
        assertNull(failure.get(), "FXML load failed: " + failure.get());
        Platform.exit();
    }
}
