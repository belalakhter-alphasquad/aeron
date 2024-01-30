package playground.app;

public class Main {
    public static void main(final String[] args) {
        ClusterService clusterService = new ClusterService();
        clusterService.RunClusterNode();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ClusterClient clusterClient = new ClusterClient();
        SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
        String messageSent = sendMessages.sendCustomMessage();
        System.out.println("Sent from Client " + messageSent + "\n");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        clusterClient.close();
        clusterService.close();

    }
}
