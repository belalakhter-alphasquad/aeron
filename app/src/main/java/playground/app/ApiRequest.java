package playground.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;

public class ApiRequest extends Thread {
    private final String gatewayUrl;
    private final int numberOfCalls;

    public ApiRequest(String gatewayUrl, int numberOfCalls) {
        this.gatewayUrl = gatewayUrl;
        this.numberOfCalls = numberOfCalls;
    }

    @Override
    public void run() {
        HttpClient client = HttpClient.newHttpClient();

        for (int i = 0; i < numberOfCalls; i++) {
            try {
                System.out.println("Making API Request to Gateway\n");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(gatewayUrl))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("Place the order", StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

                System.out.println("Response from server: " + response.body());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
