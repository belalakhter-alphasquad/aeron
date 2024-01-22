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
                int j = 1;
                String jProp = System.getProperty("j");
                if (jProp != null) {
                    try {
                        j = Integer.parseInt(jProp);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid format for jValue, using default.");
                    }
                }
                int i;
                for (i = 0; i < j; i++) {
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
