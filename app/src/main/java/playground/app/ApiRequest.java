package playground.app;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ApiRequest extends Thread {
    private final String gatewayUrl;
    private final int numberOfCalls;

    public ApiRequest(String gatewayUrl, int numberOfCalls) {
        this.gatewayUrl = gatewayUrl;
        this.numberOfCalls = numberOfCalls;
    }

    @Override
    public void run() {
        for (int i = 0; i < numberOfCalls; i++) {
            try {
                URL url = new URL(gatewayUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = "Place the order".getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Response from server: " + response.toString());
                }
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
