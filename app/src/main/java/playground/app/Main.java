package playground.app;

public class Main {

    public static void main(final String[] args) {

        // ClusterService clusterService = new ClusterService();
        // clusterService.SingleNodeCluster();

        // clusterClient = new ClusterClient();

        Thread gatewayThread = new Thread(() -> {
            try {
                new Gateway();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        gatewayThread.start();

        int number_of_messages = 1;
        String number_of_messages_Prop = System.getProperty("number_of_messages");
        String gatewayUrl = "http://localhost:3000/placeOrder";
        int numberOfCalls = 5;
        ApiRequest apiRequest = new ApiRequest(gatewayUrl, numberOfCalls);
        apiRequest.start();

        if (number_of_messages_Prop != null) {
            try {
                number_of_messages = Integer.parseInt(number_of_messages_Prop);
                System.out.println(number_of_messages);
            } catch (NumberFormatException e) {
                System.err.println("Invalid format for number of message parameter, using default.");
            }
        }

        gatewayThread.interrupt();

    }
}
