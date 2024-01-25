package playground.app;

public class Main {
    public static void main(final String[] args) {

        try {
            ClusterClient clusterClient = new ClusterClient();

            clusterClient.makeClusterAlive();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println(" Closing cluster client... \n");
                clusterClient.close();
            }));

            try {
                new Gateway();
                int number_of_messages = 1;
                String number_of_messages_Prop = System.getProperty("number_of_messages");
                String gatewayUrl = "http://localhost:3000/placeOrder";
                int numberOfCalls = 5;
                ApiRequest apiRequest = new ApiRequest(gatewayUrl, numberOfCalls);
                apiRequest.start();

                if (number_of_messages_Prop != null) {
                    try {
                        number_of_messages = Integer.parseInt(number_of_messages_Prop);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid format for number of message paramter, using default.");
                    }
                }
                int i;
                for (i = 0; i < number_of_messages; i++) {
                    Thread.sleep(20);
                    SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
                    String messageSent = sendMessages.sendCustomMessage();
                    System.out.println("Custom message sent: " + messageSent + "\n");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
