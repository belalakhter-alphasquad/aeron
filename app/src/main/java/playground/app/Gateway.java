package playground.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Gateway {
    private HttpServer server;

    public Gateway(ClusterClient clusterClient) {
        try {
            server = HttpServer.create(new InetSocketAddress(3000), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.createContext("/placeOrder", new PlaceOrderHandler(clusterClient));
        server.setExecutor(null);
        server.start();

    }

    public void Close() {
        System.out.println("Closing http server");
        server.stop(2);
    }

    public HttpServer getServer() {
        return this.server;

    }

    private static class PlaceOrderHandler implements HttpHandler {
        private final ClusterClient clusterClient;

        public PlaceOrderHandler(ClusterClient clusterClient) {
            this.clusterClient = clusterClient;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
            long SystemOrderIdget = json.get("OrderId").getAsLong();
            String Symbolget = json.get("Symbol").getAsString();
            long Quantityget = json.get("Quantity").getAsLong();
            SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
            String messageSent = sendMessages.sendCustomMessage(SystemOrderIdget, Symbolget, Quantityget);
            System.out.println("Custom message sent: " + messageSent + "\n");
            String response = "Your Order is Placed";
            exchange.sendResponseHeaders(200, response.length());

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}