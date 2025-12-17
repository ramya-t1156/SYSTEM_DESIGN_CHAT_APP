import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class ChatServer {

    static class Message {
        String from;
        String to;
        String text;

        Message(String from, String to, String text) {
            this.from = from;
            this.to = to;
            this.text = text;
        }
    }

    static List<Message> messages = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/send", new SendHandler());
        server.createContext("/messages", new MessageHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Chat Server running at http://localhost:8080");
    }

    static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class SendHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) return;

            enableCORS(exchange);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody())
            );
            String body = br.readLine();

            Map<String, String> data = parse(body);

            messages.add(new Message(
                    data.get("from"),
                    data.get("to"),
                    data.get("text")
            ));

            String response = "Message Sent";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    static class MessageHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            enableCORS(exchange);

            StringBuilder response = new StringBuilder();

            for (Message m : messages) {
                response.append(m.from)
                        .append(" → ")
                        .append(m.to)
                        .append(" : ")
                        .append(m.text)
                        .append("\n");
            }

            byte[] bytes = response.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }

    static Map<String, String> parse(String body) {
        Map<String, String> map = new HashMap<>();

        if (body == null || body.isEmpty()) return map;

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(
                        kv[0],
                        kv[1].replace("+", " ").replace("%20", " ")
                );
            }
        }
        return map;
    }
}