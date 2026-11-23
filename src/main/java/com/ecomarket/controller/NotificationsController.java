package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.AppNotification;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/** Notifications grouped by day, with an All/Unread tab row. */
public class NotificationsController {

    @FXML private ListView<AppNotification> todayList;
    @FXML private ListView<AppNotification> yesterdayList;
    @FXML private ToggleButton allToggle;
    @FXML private ToggleButton unreadToggle;

    private final MockDataService service = MockDataService.getInstance();
    private boolean unreadOnly;

    @FXML
    private void initialize() {
        allToggle.setSelected(true);
        configureCellFactory(todayList);
        configureCellFactory(yesterdayList);
        refresh();
    }

    private void configureCellFactory(ListView<AppNotification> list) {
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(AppNotification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                HBox row = new HBox(10);
                row.getStyleClass().add("notification-cell");
                if (!item.isRead()) {
                    Region dot = new Region();
                    dot.getStyleClass().add("unread-dot");
                    row.getChildren().add(dot);
                }

                VBox text = new VBox(3);
                Label title = new Label(item.getTitle());
                title.getStyleClass().add("notification-title");
                title.setWrapText(true);
                HBox meta = new HBox(10);
                Label detail = new Label(item.getDetail());
                detail.getStyleClass().add("notification-detail");
                Label time = new Label(item.getTimeLabel());
                time.getStyleClass().add("notification-time");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                meta.getChildren().addAll(detail, spacer, time);
                text.getChildren().addAll(title, meta);
                HBox.setHgrow(text, Priority.ALWAYS);
                row.getChildren().add(text);

                setGraphic(row);
                setText(null);
                setOnMouseClicked(e -> open(item));
            }
        });
    }

    private void open(AppNotification notification) {
        service.markNotificationRead(notification);
        refresh();

        if (notification.getConversationId() != null) {
            service.getConversationById(notification.getConversationId())
                    .ifPresent(c -> SceneNavigator.navigateWithContext("messages", c));
        } else if (notification.getListingId() != null) {
            service.getListingById(notification.getListingId())
                    .ifPresent(UiUtil::showListingDetails);
        }
    }

    private void refresh() {
        List<AppNotification> all = service.getNotifications();
        ObservableList<AppNotification> today = FXCollections.observableArrayList(
                all.stream().filter(n -> "Today".equals(n.getGroup())).filter(this::visible).toList());
        ObservableList<AppNotification> yesterday = FXCollections.observableArrayList(
                all.stream().filter(n -> "Yesterday".equals(n.getGroup())).filter(this::visible).toList());
        todayList.setItems(today);
        yesterdayList.setItems(yesterday);
    }

    private boolean visible(AppNotification notification) {
        return !unreadOnly || !notification.isRead();
    }

    @FXML
    private void onAll() {
        unreadOnly = false;
        allToggle.setSelected(true);
        unreadToggle.setSelected(false);
        refresh();
    }

    @FXML
    private void onUnread() {
        unreadOnly = true;
        allToggle.setSelected(false);
        unreadToggle.setSelected(true);
        refresh();
    }
}
