package util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_URL = "http://localhost:8000";

    // Send a message to the server
    public void sendMessage(String sender, String message) throws IOException {
        URL url = new URL(SERVER_URL + "/sendMessage");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Sender", sender);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = message.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
    }

    // Receive messages from the server based on timestamp and receiver
    public String receiveMessages(String receiver, long lastMessageTimestamp) throws IOException {
        URL url = new URL(SERVER_URL + "/getMessages?timestamp=" + lastMessageTimestamp);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Receiver", receiver);

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
