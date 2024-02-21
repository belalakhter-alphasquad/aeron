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
            long systemOrderIdGet = json.get("OrderId").getAsLong();
            String symbolGet = json.get("Symbol").getAsString();
            long quantityGet = json.get("Quantity").getAsLong();
            SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
            String messageSent = sendMessages.sendCustomMessage(systemOrderIdGet, symbolGet, quantityGet);
            System.out.println("From gateway: " + messageSent + "\n");

            long startTime = System.currentTimeMillis();
            long timeout = 2000;
            OrderBook.OrderDetails orderDetails = null;
            while (System.currentTimeMillis() - startTime < timeout) {
                orderDetails = OrderBook.getOrder(messageSent);
                if (orderDetails != null) {

                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String response;
            int statusCode;
            if (orderDetails != null) {
                response = "Order details: " + orderDetails.getOrderId() + ", " +
                        orderDetails.getSymbol() + ", " + orderDetails.getSize();
                statusCode = 200;
            } else {
                response = "Order not found within the timeout period";
                statusCode = 400;
            }
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

    }
}