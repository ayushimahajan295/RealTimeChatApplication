package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (authenticate(username, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatWindow.fxml"));
                Parent root = loader.load();
                ChatController chatController = loader.getController();
                chatController.setUsername(username);

                Stage stage = new Stage();
                stage.setTitle("Chat - " + username);
                stage.setScene(new Scene(root));
                stage.show();

                Stage currentStage = (Stage) usernameField.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                showError("An error occurred while loading the chat window.");
                e.printStackTrace();
            }
        } else {
            showError("Invalid username or password. Please try again.");
        }
    }

    private boolean authenticate(String username, String password) {
        return (username.equals("user1") || username.equals("user2")) && password.equals("password");
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
