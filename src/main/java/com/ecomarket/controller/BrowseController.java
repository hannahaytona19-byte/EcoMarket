package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.Category;
import com.ecomarket.model.Listing;
import com.ecomarket.model.ProductCondition;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Buyer home. Category chips, attribute dropdowns and the shared search field
 * narrow the mock listing grid; opening a card shows listing details.
 */
public class BrowseController {

    private static final String ALL = "All";
    private static final String ANY = "Any";

    // Ranges follow the price scale in schema.sql's product seed rows.
    private static final String PRICE_UNDER_20 = "Under Php 20";
    private static final String PRICE_20_40 = "Php 20–40";
    private static final String PRICE_40_70 = "Php 40–70";
    private static final String PRICE_ABOVE_70 = "Above Php 70";

    private static final double CARD_GAP = 16;
    private static final double MIN_CARD_WIDTH = 220;

    @FXML private FlowPane filterChips;
    @FXML private ComboBox<String> conditionCombo;
    @FXML private ComboBox<String> priceCombo;
    @FXML private ComboBox<String> locationCombo;
    @FXML private FlowPane productFlow;
    @FXML private ScrollPane scrollPane;
    @FXML private Label resultLabel;
    @FXML private Label emptyLabel;

    private final MockDataService service = MockDataService.getInstance();
    private final Map<String, ToggleButton> chipButtons = new LinkedHashMap<>();
    private final Set<String> selectedCategories = new LinkedHashSet<>();
    private String search = "";

    @FXML
    private void initialize() {
        chipButtons.put(ALL, chip(ALL));
        for (Category category : service.getCategories()) {
            chipButtons.put(category.getName(), chip(category.getName()));
        }
        filterChips.getChildren().addAll(chipButtons.values());
        chipButtons.get(ALL).setSelected(true);

        conditionCombo.getItems().add(ANY);
        for (ProductCondition condition : ProductCondition.values()) {
            conditionCombo.getItems().add(condition.getLabel());
        }
        priceCombo.getItems().addAll(ANY, PRICE_UNDER_20, PRICE_20_40,
                PRICE_40_70, PRICE_ABOVE_70);

        // Locations are derived from the listing data, with the requested order first.
        List<String> dataLocations = service.getListings().stream()
                .map(Listing::getLocation)
                .filter(location -> location != null && !location.isBlank())
                .distinct()
                .toList();
        List<String> orderedLocations = new ArrayList<>();
        orderedLocations.add(ANY);
        List.of("Cavite", "Taguig", "Batangas").stream()
                .filter(dataLocations::contains)
                .forEach(orderedLocations::add);
        dataLocations.stream()
                .filter(location -> !orderedLocations.contains(location))
                .forEach(orderedLocations::add);
        locationCombo.getItems().addAll(orderedLocations);

        conditionCombo.setValue(ANY);
        priceCombo.setValue(ANY);
        locationCombo.setValue(ANY);

        conditionCombo.setOnAction(e -> refresh());
        priceCombo.setOnAction(e -> refresh());
        locationCombo.setOnAction(e -> refresh());

        Object context = SceneNavigator.consumeContext();
        if (context instanceof String value && !value.isBlank()) {
            if (chipButtons.containsKey(value)) {
                chipButtons.get(value).setSelected(true);
                onChip(value);
            } else {
                search = value;
            }
        }

        scrollPane.widthProperty().addListener((obs, oldWidth, newWidth) -> layoutCards());
        refresh();
    }

    private ToggleButton chip(String name) {
        ToggleButton chip = new ToggleButton(name);
        chip.getStyleClass().add("chip");
        chip.setFocusTraversable(true);
        chip.setOnAction(e -> onChip(name));
        return chip;
    }

    private void onChip(String name) {
        if (ALL.equals(name)) {
            selectedCategories.clear();
            chipButtons.forEach((key, button) -> button.setSelected(ALL.equals(key)));
        } else {
            ToggleButton button = chipButtons.get(name);
            if (button != null && button.isSelected()) {
                selectedCategories.add(name);
            } else {
                selectedCategories.remove(name);
            }
            chipButtons.get(ALL).setSelected(selectedCategories.isEmpty());
        }
        refresh();
    }

    /** Called by the shared top-bar search field. */
    public void applySearch(String text) {
        search = text == null ? "" : text.trim();
        refresh();
    }

    private void refresh() {
        List<Listing> filtered = service.getListings().stream()
                .filter(this::matchesSearch)
                .filter(this::matchesCategory)
                .filter(this::matchesCondition)
                .filter(this::matchesPrice)
                .filter(this::matchesLocation)
                .toList();

        productFlow.getChildren().clear();
        for (Listing listing : filtered) {
            productFlow.getChildren().add(UiUtil.listingCard(listing, this::openDetails));
        }
        layoutCards();

        boolean empty = filtered.isEmpty();
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);

        String suffix = search.isBlank() ? "" : " for \"" + search + "\"";
        resultLabel.setText(filtered.size() + (filtered.size() == 1 ? " item" : " items")
                + " found" + suffix);
    }

    /** Reflows cards like CSS grid's repeat(auto-fill, minmax(220px, 1fr)). */
    private void layoutCards() {
        double available = Math.max(MIN_CARD_WIDTH, scrollPane.getWidth() - 52);
        productFlow.setPrefWrapLength(available);

        int columns = Math.max(1,
                (int) Math.floor((available + CARD_GAP) / (MIN_CARD_WIDTH + CARD_GAP)));
        double cardWidth = Math.max(1, (available - CARD_GAP * (columns - 1)) / columns);

        for (Node node : productFlow.getChildren()) {
            if (node instanceof VBox card) {
                card.setPrefWidth(cardWidth);
            }
        }
    }

    private boolean matchesSearch(Listing listing) {
        if (search.isBlank()) return true;
        String query = search.toLowerCase();
        String category = listing.getCategory() == null ? "" : listing.getCategory().getName();
        return contains(listing.getTitle(), query)
                || contains(listing.getDescription(), query)
                || contains(listing.getLocation(), query)
                || contains(category, query);
    }

    private boolean matchesCategory(Listing listing) {
        if (selectedCategories.isEmpty()) return true;
        return selectedCategories.stream().anyMatch(category -> listing.getCategory() != null
                && listing.getCategory().getName().equalsIgnoreCase(category));
    }

    private boolean matchesCondition(Listing listing) {
        String condition = conditionCombo.getValue();
        if (condition == null || ANY.equals(condition)) return true;
        return condition.equalsIgnoreCase(
                listing.getCondition() == null ? "" : listing.getCondition().getLabel());
    }

    private boolean matchesPrice(Listing listing) {
        String price = priceCombo.getValue();
        if (price == null || ANY.equals(price)) return true;
        double value = listing.getPrice();
        return switch (price) {
            case PRICE_UNDER_20 -> value < 20;
            case PRICE_20_40 -> value >= 20 && value <= 40;
            case PRICE_40_70 -> value > 40 && value <= 70;
            case PRICE_ABOVE_70 -> value > 70;
            default -> true;
        };
    }

    private boolean matchesLocation(Listing listing) {
        String location = locationCombo.getValue();
        if (location == null || ANY.equals(location)) return true;
        return location.equalsIgnoreCase(listing.getLocation());
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private void openDetails(Listing listing) {
        UiUtil.showListingDetails(listing);
    }
}
