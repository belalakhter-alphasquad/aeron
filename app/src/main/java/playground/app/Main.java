package playground.app;

public class Main {
    public static void main(final String[] args) {
        ClusterClient clusterClient = null;

        try {
            clusterClient = new ClusterClient();
            System.out.println("Cluster connection succeeded!");
            clusterClient.sendKeepAlive();
            final ClusterClient finalClusterClient = clusterClient;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println(" Closing cluster client...");
                finalClusterClient.close();
            }));

            try {
                Thread.sleep(5000);
                SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
                boolean messageSent = sendMessages.sendCustomMessage();
                System.out.println("Custom message sent: " + messageSent);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
