package io.ecomarket.desktop.ui.dialog;

import io.ecomarket.desktop.repository.CategoryRepository;
import io.ecomarket.desktop.repository.ProductRepository;
import io.ecomarket.desktop.schema.entity.Category;
import io.ecomarket.desktop.schema.entity.Product;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.ProductCondition;
import io.ecomarket.desktop.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.UUID;

public class AddProductDialog extends Dialog<Product> {

    public AddProductDialog(CategoryRepository categoryRepo, ProductRepository productRepo) {
        setTitle("List Sustainable Product — ecoMarket");
        setHeaderText("Publish an eco-friendly, upcycled, or refurbished product");

        ButtonType publishBtnType = new ButtonType("Publish Product", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(publishBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("e.g. Upcycled Denim Backpack");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe materials, sourcing, and lifecycle...");
        descArea.setPrefRowCount(3);

        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 24.50");

        TextField stockField = new TextField("10");

        ComboBox<ProductCondition> conditionCombo = new ComboBox<>();
        conditionCombo.getItems().setAll(ProductCondition.values());
        conditionCombo.setValue(ProductCondition.UPCYCLED);

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().setAll(categoryRepo.findAll());
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.setValue(categoryCombo.getItems().get(0));
        }

        TextField ecoBadgeField = new TextField();
        ecoBadgeField.setPromptText("e.g. Plastic-Free / Solar Crafted");

        TextField carbonField = new TextField("3.50");
        carbonField.setPromptText("kg CO2 saved vs standard product");

        TextField imgUrlField = new TextField();
        imgUrlField.setPromptText("https://images.unsplash.com/...");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Condition:"), 0, 2);
        grid.add(conditionCombo, 1, 2);
        grid.add(new Label("Price ($):"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Eco Badge:"), 0, 5);
        grid.add(ecoBadgeField, 1, 5);
        grid.add(new Label("CO2 Saved (kg):"), 0, 6);
        grid.add(carbonField, 1, 6);
        grid.add(new Label("Description:"), 0, 7);
        grid.add(descArea, 1, 7);
        grid.add(new Label("Image URL:"), 0, 8);
        grid.add(imgUrlField, 1, 8);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == publishBtnType) {
                try {
                    User seller = AuthService.getInstance().getCurrentUser();
                    BigDecimal price = new BigDecimal(priceField.getText().trim());
                    int stock = Integer.parseInt(stockField.getText().trim());
                    BigDecimal carbon = new BigDecimal(carbonField.getText().trim());

                    Product p = new Product(
                            UUID.randomUUID().toString(),
                            seller,
                            categoryCombo.getValue(),
                            titleField.getText().trim(),
                            descArea.getText().trim(),
                            price,
                            stock,
                            conditionCombo.getValue(),
                            ecoBadgeField.getText().trim().isBlank() ? "Eco Certified" : ecoBadgeField.getText().trim(),
                            carbon,
                            imgUrlField.getText().trim()
                    );
                    productRepo.save(p);
                    return p;
                } catch (Exception ex) {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Invalid input: " + ex.getMessage());
                    err.showAndWait();
                    return null;
                }
            }
            return null;
        });
    }
}
