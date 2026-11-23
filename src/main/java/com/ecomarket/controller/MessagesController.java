package com.ecomarket.controller;

import com.ecomarket.app.SceneNavigator;
import com.ecomarket.model.Conversation;
import com.ecomarket.model.Listing;
import com.ecomarket.model.Message;
import com.ecomarket.service.MockDataService;
import com.ecomarket.util.UiUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Two-panel chat view. "Suggest meetup" only inserts a message. */
public class MessagesController {

    @FXML private TextField chatSearchField;
    @FXML private ListView<Conversation> conversationList;
    @FXML private Label headerAvatar;
    @FXML private Label headerName;
    @FXML private Label headerStatus;
    @FXML private VBox pinnedBox;
    @FXML private Label pinnedTitle;
    @FXML private Label pinnedPrice;
    @FXML private ScrollPane messageScroll;
    @FXML private VBox messageBox;
    @FXML private TextField messageField;
    @FXML private javafx.scene.control.Button meetupButton;

    private final MockDataService service = MockDataService.getInstance();
    private Conversation selected;
    private boolean updatingList;

    @FXML
    private void initialize() {
        Object context = SceneNavigator.consumeContext();
        if (context instanceof Conversation conversation) {
            selected = conversation;
        } else if (context instanceof Listing listing) {
            selected = service.findConversationForListing(listing).orElse(null);
        }

        meetupButton.setTooltip(new Tooltip("Insert a meetup suggestion as a message only"));
        conversationList.setCellFactory(view -> new ConversationCell());
        conversationList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, value) -> {
                    if (!updatingList && value != null) selectConversation(value);
                });
        chatSearchField.textProperty().addListener((obs, old, value) -> refreshConversations());

        pinnedBox.setOnMouseClicked(e -> {
            if (selected != null && selected.getListing() != null) {
                UiUtil.showListingDetails(selected.getListing());
            }
        });

        refreshConversations();
        if (selected == null && !service.getConversations().isEmpty()) {
            selected = service.getConversations().get(0);
        }
        if (selected != null) {
            selectConversation(selected);
        }
    }

    private void refreshConversations() {
        String q = chatSearchField.getText() == null ? "" : chatSearchField.getText().toLowerCase();
        var filtered = service.getConversations().stream()
                .filter(c -> q.isBlank()
                        || (c.getParticipant() != null && c.getParticipant().fullName().toLowerCase().contains(q))
                        || c.lastPreview().toLowerCase().contains(q))
                .toList();

        updatingList = true;
        try {
            conversationList.setItems(FXCollections.observableArrayList(filtered));
            if (selected != null && filtered.contains(selected)) {
                conversationList.getSelectionModel().select(selected);
            }
        } finally {
            updatingList = false;
        }
    }

    private void selectConversation(Conversation conversation) {
        selected = conversation;
        service.markConversationRead(conversation);
        headerAvatar.setText(conversation.getParticipant() == null
                ? "?" : conversation.getParticipant().initials());
        headerAvatar.setAlignment(javafx.geometry.Pos.CENTER);
        headerAvatar.setPrefSize(44, 44);
        headerAvatar.setMinSize(44, 44);
        headerAvatar.setStyle("-fx-font-size: 15px;");
        headerName.setText(conversation.getParticipant() == null
                ? "Unknown" : conversation.getParticipant().fullName());
        headerStatus.setText("Chat about a listing • " + conversation.getMessages().size() + " messages");

        Listing listing = conversation.getListing();
        pinnedTitle.setText(listing == null ? "No listing attached" : listing.getTitle());
        pinnedPrice.setText(listing == null ? "" : UiUtil.currency(listing.getPrice()));

        renderMessages();
        refreshConversations();
        messageField.requestFocus();
    }

    private void renderMessages() {
        messageBox.getChildren().clear();
        if (selected == null) return;

        for (Message message : selected.getMessages()) {
            boolean mine = message.isMine(service.getCurrentUser());
            Label bubble = new Label(message.getText());
            bubble.getStyleClass().addAll("bubble", mine ? "bubble-out" : "bubble-in");
            bubble.setWrapText(true);
            bubble.setMaxWidth(430);

            Label time = new Label(message.timeLabel());
            time.getStyleClass().add("message-time");

            VBox stack = new VBox(2, bubble, time);
            stack.getStyleClass().add("message-row");
            HBox.setHgrow(stack, Priority.NEVER);
            if (mine) {
                stack.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                HBox row = new HBox();
                row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(spacer, stack);
                messageBox.getChildren().add(row);
            } else {
                stack.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                HBox row = new HBox(stack);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                messageBox.getChildren().add(row);
            }
        }
        Platform.runLater(() -> messageScroll.setVvalue(1.0));
    }

    @FXML
    private void onSend() {
        if (selected == null) return;
        String text = messageField.getText();
        if (text == null || text.isBlank()) return;
        service.addMessage(selected, text);
        messageField.clear();
        renderMessages();
        refreshConversations();
        messageField.requestFocus();
    }

    @FXML
    private void onEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onSend();
            event.consume();
        }
    }

    @FXML
    private void onSuggestMeetup() {
        if (selected == null) return;
        service.addMeetupMessage(selected);
        renderMessages();
        refreshConversations();
    }

    /** Custom chat row: avatar, name, last-message preview, time, unread count. */
    private final class ConversationCell extends ListCell<Conversation> {
        private final HBox root = new HBox(10);
        private final Label avatar = new Label();
        private final VBox text = new VBox(3);
        private final Label name = new Label();
        private final Label preview = new Label();
        private final Label time = new Label();
        private final Label badge = new Label();

        private ConversationCell() {
            root.getStyleClass().add("conversation-cell");
            avatar.getStyleClass().add("avatar");
            avatar.setPrefSize(40, 40);
            avatar.setMinSize(40, 40);
            avatar.setAlignment(javafx.geometry.Pos.CENTER);
            avatar.setStyle("-fx-font-size: 13px;");
            name.getStyleClass().add("chat-name");
            preview.getStyleClass().add("chat-preview");
            time.getStyleClass().add("chat-time");
            badge.getStyleClass().add("unread-badge");
            HBox meta = new HBox(8, time, badge);
            text.getChildren().addAll(name, preview);
            HBox.setHgrow(text, Priority.ALWAYS);
            root.getChildren().addAll(avatar, text, meta);
            setText(null);
        }

        @Override
        protected void updateItem(Conversation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            avatar.setText(item.getParticipant() == null ? "?" : item.getParticipant().initials());
            name.setText(item.getParticipant() == null ? "Unknown" : item.getParticipant().fullName());
            preview.setText(item.lastPreview());
            time.setText(item.lastTimeLabel());
            badge.setText(item.getUnreadCount() > 0 ? String.valueOf(item.getUnreadCount()) : "");
            badge.setVisible(item.getUnreadCount() > 0);
            badge.setManaged(item.getUnreadCount() > 0);
            setGraphic(root);
            setOnMouseClicked(e -> selectConversation(item));
        }
    }
}
