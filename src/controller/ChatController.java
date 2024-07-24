package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import util.ChatClient;
import util.EncryptionUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController {
    @FXML
    private ListView<Message> messageListView;

    @FXML
    private TextField messageTextField;

    private ChatClient chatClient;
    private String username;
    private Timer timer;
    private long lastMessageTimestamp;

    @FXML
    private void initialize() {
        chatClient = new ChatClient();
        startMessagePolling();
        lastMessageTimestamp = System.currentTimeMillis();
        messageListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.getText() != null) {
                        setText(item.getSender() + ": " + item.getText());
                        setAlignment(item.getSender().equals(username) ?
                                javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
                    } else if (item.getImage() != null) {
                        setText(item.getSender() + ": [Image]");
                        ImageView imageView = new ImageView(item.getImage());
                        imageView.setFitWidth(200);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                        setAlignment(item.getSender().equals(username) ?
                                javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
                    }
                }
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void handleSendMessage() {
        String message = messageTextField.getText();
        if (!message.isEmpty()) {
            String formattedMessage = username + ": " + message;
            String encryptedMessage = EncryptionUtil.encrypt(formattedMessage);
            try {
                chatClient.sendMessage(username, encryptedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageListView.getItems().add(new Message(username, message));
            messageTextField.clear();
        }
    }

    @FXML
    private void handleSendImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String formattedMessage = username + ": [Image] " + base64Image;
                String encryptedMessage = EncryptionUtil.encrypt(formattedMessage);
                chatClient.sendMessage(username, encryptedMessage);
                messageListView.getItems().add(new Message(username, new Image(new ByteArrayInputStream(imageBytes))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startMessagePolling() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchMessages();
            }
        }, 0, 2000);
    }

    private void fetchMessages() {
        new Thread(() -> {
            try {
                String messages = chatClient.receiveMessages(username, lastMessageTimestamp);
                if (messages != null && !messages.isEmpty()) {
                    String[] lines = messages.split("\n");
                    for (String encryptedMessage : lines) {
                        final String decryptedMessage = EncryptionUtil.decrypt(encryptedMessage);
                        Platform.runLater(() -> {
                            if (decryptedMessage.contains("[Image]")) {
                                String[] parts = decryptedMessage.split(" ", 3);
                                String sender = parts[0];
                                String base64Image = parts[2];
                                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                Image image = new Image(new ByteArrayInputStream(imageBytes));
                                messageListView.getItems().add(new Message(sender, image));
                            } else {
                                String[] parts = decryptedMessage.split(": ", 2);
                                String sender = parts[0];
                                String message = parts[1];
                                messageListView.getItems().add(new Message(sender, message));
                            }
                        });
                    }
                    // Update the last message timestamp
                    lastMessageTimestamp = System.currentTimeMillis();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static class Message {
        private final String sender;
        private final String text;
        private final Image image;

        public Message(String sender, String text) {
            this.sender = sender;
            this.text = text;
            this.image = null;
        }

        public Message(String sender, Image image) {
            this.sender = sender;
            this.image = image;
            this.text = null;
        }

        public String getSender() {
            return sender;
        }

        public String getText() {
            return text;
        }

        public Image getImage() {
            return image;
        }
    }
}
