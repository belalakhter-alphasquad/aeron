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
                Thread.sleep(2000);
                SendMessages sendMessages = new SendMessages(clusterClient.getAeronCluster());
                String messageSent = sendMessages.sendCustomMessage();
                System.out.println("Custom message sent: " + messageSent + "\n");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
