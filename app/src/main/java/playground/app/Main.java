package playground.app;

public class Main {
    public static void main(final String[] args) {
        ClusterClient clusterClient = null;

        try {
            clusterClient = new ClusterClient();
            System.out.println("Cluster connection succeeded!");

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (clusterClient != null) {
                clusterClient.close();
            }
        }
    }
}
