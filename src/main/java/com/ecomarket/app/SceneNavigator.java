package com.ecomarket.app;

import com.ecomarket.controller.AuthLayoutController;
import com.ecomarket.controller.ShellController;
import com.ecomarket.model.User;
import com.ecomarket.service.MockDataService;
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
            Map.entry("login", "auth-layout.fxml"),
            Map.entry("register", "auth-layout.fxml"),
            Map.entry("auth-layout", "auth-layout.fxml"),
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

    /** Both auth routes share one mounted layout so the panels can slide. */
    private static final Set<String> AUTH_ROUTES = Set.of("login", "register");

    private static Stage stage;
    private static Scene scene;
    private static Parent shellRoot;
    private static ShellController shell;
    private static Parent authRoot;
    private static AuthLayoutController auth;
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
        if (AUTH_ROUTES.contains(route)) {
            if (authRoot == null || scene.getRoot() != authRoot) {
                View authView = loadView(route);
                auth = (AuthLayoutController) authView.controller();
                authRoot = authView.root();
                scene.setRoot(authRoot);
            }
            // Same root: only the mode changes, so the slide/fade can run.
            auth.show(route);
        } else if (SHELL_ROUTES.contains(route)) {
            if (shell == null || shellRoot == null || scene.getRoot() != shellRoot) {
                String initialRole = initialShellRole(route);
                View shellView = loadView("shell");
                shell = (ShellController) shellView.controller();
                shellRoot = shellView.root();
                shellRole = initialRole;
                shell.setRole(initialRole);
                scene.setRoot(shellRoot);
            }
            shell.show(route);
        } else {
            View view = loadView(route);
            scene.setRoot(view.root());
        }
        currentRoute = route;
    }

    /** The shell's role is chosen once, from the active session or target route. */
    private static String initialShellRole(String route) {
        if ("seller".equals(route)) {
            return "SELLER";
        }
        User user = MockDataService.getInstance().getCurrentUser();
        return user != null && user.getRole() == User.Role.SELLER ? "SELLER" : "BUYER";
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
