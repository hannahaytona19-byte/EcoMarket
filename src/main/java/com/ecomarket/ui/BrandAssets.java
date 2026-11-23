package com.ecomarket.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single source of truth for the EcoMarket brand artwork, sizes and colours.
 *
 * <p>All app screens go through this class. To swap the logo later, replace the
 * files under {@code /assets/images} and {@code /assets/icons}; no FXML or
 * controller needs to change.</p>
 */
public final class BrandAssets {

    public static final String APP_NAME = "EcoMarket";
    public static final String TAGLINE = "Reuse. Resell. Rehome.";
    public static final String APP_TITLE = APP_NAME + " — " + TAGLINE;

    /** Colours sampled from the supplied badge. */
    public static final String PRIMARY_GREEN = "#2E9E1F";
    public static final String DARK_GREEN = "#0B4D12";
    public static final String CREAM = "#F3EBC0";

    /** Logical (density-independent) placements. */
    public static final int SIZE_TINY = 32;
    public static final int SIZE_HEADER = 40;
    public static final int SIZE_FOOTER = 40;
    public static final int SIZE_ROLE = 128;
    public static final int SIZE_ABOUT = 128;
    public static final int SIZE_LOGIN = 160;
    public static final int SIZE_SPLASH = 256;
    public static final int SIZE_WINDOW = 64;

    public static final String IMAGE_FOLDER = "/assets/images/";
    public static final String ICON_FOLDER = "/assets/icons/";
    public static final String ICO_PATH = ICON_FOLDER + "ecomarket.ico";
    public static final String ICNS_PATH = ICON_FOLDER + "ecomarket.icns";

    private static final List<Integer> GENERATED_SIZES = List.of(16, 32, 48, 64, 128, 256, 512);
    private static final String MASTER_512 = IMAGE_FOLDER + "ecomarket-logo-512.png";
    private static final String MASTER_2X = IMAGE_FOLDER + "ecomarket-logo@2x.png";
    private static final String APP_VERSION = resolveVersion();
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private BrandAssets() {
    }

    /** Version from the JAR manifest, with a sensible fallback for `mvn javafx:run`. */
    public static String appVersion() {
        return APP_VERSION;
    }

    /**
     * Returns the best bundled asset for a logical size on the current display.
     * The source badge is square and is only ever uniformly scaled.
     */
    public static String logoPath(int logicalSize) {
        double target = Math.max(16, logicalSize * outputScale());
        if (target > 512) {
            return MASTER_2X;
        }
        for (int size : GENERATED_SIZES) {
            if (size >= target) {
                return IMAGE_FOLDER + "ecomarket-logo-" + size + ".png";
            }
        }
        return MASTER_512;
    }

    public static Image image(int logicalSize) {
        return load(logoPath(logicalSize));
    }

    /**
     * Creates a fresh ImageView (each node needs its own instance) sized in
     * logical pixels. preserveRatio is always on, so the badge never distorts.
     */
    public static ImageView imageView(int logicalSize) {
        ImageView view = new ImageView(image(logicalSize));
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(logicalSize);
        view.setFitHeight(logicalSize);
        return view;
    }

    /** Platform window/taskbar icon; falls back to PNG if a decoder is unavailable. */
    public static Image windowIcon() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String preferred = ICO_PATH;
        if (os.contains("mac")) {
            preferred = ICNS_PATH;
        } else if (!os.contains("win")) {
            preferred = IMAGE_FOLDER + "ecomarket-logo-128.png";
        }
        try {
            return load(preferred);
        } catch (RuntimeException unsupportedByThisJfxBuild) {
            return image(SIZE_WINDOW);
        }
    }

    public static Image load(String resourcePath) {
        return IMAGE_CACHE.computeIfAbsent(resourcePath, path -> {
            URL url = BrandAssets.class.getResource(path);
            if (url == null) {
                throw new IllegalStateException("Missing brand asset: " + path);
            }
            Image loaded = new Image(url.toExternalForm());
            if (loaded.isError()) {
                throw new IllegalStateException("Could not decode brand asset: " + path, loaded.getException());
            }
            return loaded;
        });
    }

    private static double outputScale() {
        try {
            return Math.max(1.0, Screen.getPrimary().getOutputScaleX());
        } catch (Throwable headlessOrNotStarted) {
            return 1.0;
        }
    }

    private static String resolveVersion() {
        String version = BrandAssets.class.getPackage().getImplementationVersion();
        return version == null || version.isBlank() ? "1.0.0-SNAPSHOT" : version;
    }
}
