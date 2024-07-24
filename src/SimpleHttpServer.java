import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleHttpServer {
    private static final List<Message> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/sendMessage", new SendMessageHandler());
        server.createContext("/getMessages", new GetMessagesHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000...");
    }

    static class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String messageText = reader.readLine();
                String sender = exchange.getRequestHeaders().getFirst("Sender");
                long timestamp = System.currentTimeMillis();

                if (sender != null && !sender.isEmpty()) {
                    synchronized (messages) {
                        messages.add(new Message(sender, messageText, timestamp));
                    }

                    String response = "Message received";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class GetMessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                long timestamp = 0;
                String receiver = exchange.getRequestHeaders().getFirst("Receiver");

                if (query != null) {
                    String[] queryParams = query.split("&");
                    for (String param : queryParams) {
                        if (param.startsWith("timestamp=")) {
                            try {
                                timestamp = Long.parseLong(param.substring("timestamp=".length()));
                            } catch (NumberFormatException e) {
                                timestamp = 0;
                            }
                        }
                    }
                }

                if (receiver != null && !receiver.isEmpty()) {
                    String response;
                    synchronized (messages) {
                        long finalTimestamp = timestamp;
                        response = messages.stream()
                                .filter(message -> message.getTimestamp() > finalTimestamp && !message.getSender().equals(receiver))
                                .map(Message::getText)
                                .collect(Collectors.joining("\n"));
                    }

                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    private static class Message {
        private final String sender;
        private final String text;
        private final long timestamp;

        public Message(String sender, String text, long timestamp) {
            this.sender = sender;
            this.text = text;
            this.timestamp = timestamp;
        }

        public String getSender() {
            return sender;
        }

        public String getText() {
            return text;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
