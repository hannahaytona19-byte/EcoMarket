package com.ecomarket.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * The single reusable EcoMarket logo: a two-leaf green sprout above a serif
 * wordmark. The geometry mirrors {@code /img/eco-market-logo.svg}; JavaFX's
 * Image class has no built-in SVG decoder, so the same paths are drawn here
 * with SVGPath nodes. Every screen uses this component rather than duplicating
 * the artwork or the wordmark.
 */
public class LogoView extends VBox {

    private static final double SPROUT_WIDTH = 60;
    private static final double SPROUT_HEIGHT = 40;

    private final StackPane sprout = new StackPane();
    private final SVGPath stem = path("M0 20 C0 10 0 4 0 -4", null, "#2F6B2F", 2.6);
    private final SVGPath leftLeaf = path(
            "M0 8 C-12 8 -22 1 -26 -10 C-14 -13 -3 -6 0 8 Z", "#4C8C2B", "#2F6B2F", 1.1);
    private final SVGPath rightLeaf = path(
            "M0 2 C12 2 22 -5 26 -16 C14 -19 3 -12 0 2 Z", "#4C8C2B", "#2F6B2F", 1.1);
    private final Text wordmark = new Text("EcoMarket");

    public LogoView() {
        getStyleClass().add("eco-logo");
        setAlignment(Pos.CENTER);

        stem.getStyleClass().add("logo-stem");
        leftLeaf.getStyleClass().add("logo-leaf");
        rightLeaf.getStyleClass().add("logo-leaf");

        sprout.getChildren().addAll(stem, leftLeaf, rightLeaf);

        wordmark.getStyleClass().add("logo-wordmark");
        wordmark.setFont(Font.font("Georgia", FontWeight.BOLD, 22));

        getChildren().addAll(sprout, wordmark);
        setSize(22, 28);
    }

    /**
     * @param wordmarkSize serif font size in pixels
     * @param sproutHeight rendered height of the two-leaf sprout in pixels
     */
    public void setSize(double wordmarkSize, double sproutHeight) {
        double scale = sproutHeight / SPROUT_HEIGHT;
        for (SVGPath path : new SVGPath[]{stem, leftLeaf, rightLeaf}) {
            path.setScaleX(scale);
            path.setScaleY(scale);
        }

        double scaledWidth = SPROUT_WIDTH * scale;
        sprout.setPrefSize(scaledWidth, sproutHeight);
        sprout.setMinSize(scaledWidth, sproutHeight);
        sprout.setMaxSize(scaledWidth, sproutHeight);

        wordmark.setFont(Font.font("Georgia", FontWeight.BOLD, wordmarkSize));
        setSpacing(Math.max(1, wordmarkSize * 0.06));
    }

    private static SVGPath path(String content, String fill, String stroke, double strokeWidth) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(fill == null ? Color.TRANSPARENT : Color.web(fill));
        path.setStroke(stroke == null ? null : Color.web(stroke));
        path.setStrokeWidth(strokeWidth);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return path;
    }
}
