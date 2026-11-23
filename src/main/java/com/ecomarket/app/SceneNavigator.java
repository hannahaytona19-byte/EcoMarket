package com.ecomarket.app;

import com.ecomarket.controller.ShellController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Central routing utility. Full-page screens (landing, login, role select...)
 * replace the Scene root. Logged-in screens are loaded INSIDE the shared shell
 * so the sidebar and top bar stay fixed while only the center content swaps.
 */
public final class SceneNavigator {

    public record View(Parent root, Object controller) {
    }

    private static final Map<String, String> VIEWS = Map.ofEntries(
            Map.entry("landing", "landing.fxml"),
            Map.entry("role", "role-select.fxml"),
            Map.entry("login", "login.fxml"),
            Map.entry("register", "register.fxml"),
            Map.entry("shell", "shell.fxml"),
            Map.entry("browse", "browse.fxml"),
            Map.entry("notifications", "notifications.fxml"),
            Map.entry("messages", "messages.fxml"),
            Map.entry("profile", "profile.fxml"),
            Map.entry("seller", "seller-dashboard.fxml"),
            Map.entry("listing-details", "listing-details.fxml"),
            Map.entry("listing-form", "listing-form.fxml")
    );

    private static final Set<String> SHELL_ROUTES =
            Set.of("browse", "notifications", "messages", "profile", "seller");

    private static Stage stage;
    private static Scene scene;
    private static Parent shellRoot;
    private static ShellController shell;
    private static String shellRole = "BUYER";
    private static String currentRoute = "landing";
    private static Object pendingContext;

    private SceneNavigator() {
    }

    public static void init(Stage primaryStage, Scene initialScene) {
        stage = primaryStage;
        scene = initialScene;
    }

    public static Stage stage() {
        return stage;
    }

    public static Scene scene() {
        return scene;
    }

    public static String currentRoute() {
        return currentRoute;
    }

    /** True while one of the logged-in shell screens is showing. */
    public static boolean isShellActive() {
        return shellRoot != null && scene != null && scene.getRoot() == shellRoot;
    }

    /** Route used by the clickable logo: dashboard when signed in, landing otherwise. */
    public static String homeRoute() {
        if (!isShellActive()) {
            return "landing";
        }
        return "seller".equals(shellRole) ? "seller" : "browse";
    }

    public static View loadView(String route) {
        String file = VIEWS.get(route);
        if (file == null) {
            throw new IllegalArgumentException("Unknown view: " + route);
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/" + file));
            Parent root = loader.load();
            return new View(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load FXML for '" + route + "'", e);
        }
    }

    public static void navigate(String route) {
        if (SHELL_ROUTES.contains(route)) {
            String requiredRole = "seller".equals(route) ? "SELLER" : "BUYER";
            if (shell == null || shellRoot == null
                    || scene.getRoot() != shellRoot
                    || !requiredRole.equals(shellRole)) {
                View shellView = loadView("shell");
                shell = (ShellController) shellView.controller();
                shellRoot = shellView.root();
                shellRole = requiredRole;
                shell.setRole(requiredRole);
                scene.setRoot(shellRoot);
            }
            shell.show(route);
        } else {
            View view = loadView(route);
            scene.setRoot(view.root());
        }
        currentRoute = route;
    }

    public static void navigateWithContext(String route, Object context) {
        pendingContext = context;
        navigate(route);
    }

    /** Child controllers call this once in initialize() to pick up navigation data. */
    public static Object consumeContext() {
        Object value = pendingContext;
        pendingContext = null;
        return value;
    }

    public static void clearContext() {
        pendingContext = null;
    }
}
