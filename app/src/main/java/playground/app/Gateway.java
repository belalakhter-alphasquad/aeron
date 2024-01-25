package playground.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Gateway {

    public Gateway() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/placeOrder", new PlaceOrderHandler());
        server.setExecutor(null);
        server.start();
    }

    private static class PlaceOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String orderData = new String(exchange.getRequestBody().readAllBytes());
            String orderResponse = processOrder(orderData);
            exchange.sendResponseHeaders(200, orderResponse.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(orderResponse.getBytes());
            }
        }

        private String processOrder(String orderData) {
            return "Order placed: ";
        }
    }
}
