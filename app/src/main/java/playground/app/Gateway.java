package playground.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
            SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
            String messageSent = sendMessages.sendCustomMessage();
            System.out.println("Custom message sent: " + messageSent + "\n");
            String response = "Message sent to cluster service: " + messageSent;
            exchange.sendResponseHeaders(200, response.length());

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}