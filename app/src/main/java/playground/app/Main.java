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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
