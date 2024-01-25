package playground.app;

public class Main {
    public static void main(final String[] args) {
        try {
            Thread clusterThread = new Thread(() -> {
                try (ClusterService singleNodeCluster = new ClusterService()) {
                    singleNodeCluster.SingleNodeCluster();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            clusterThread.start();
            Thread.sleep(10000);
            ClusterClient clusterClient = new ClusterClient();
            Thread gatewayThread = new Thread(() -> {
                try {
                    new Gateway();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            gatewayThread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Closing cluster client...\n");
                clusterClient.close();

                System.out.println("Closing gateway...\n");
            }));

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
