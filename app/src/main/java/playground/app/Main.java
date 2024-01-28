package playground.app;

public class Main {

    public static void main(final String[] args) {

        ClusterService clusterService = new ClusterService();
        clusterService.SingleNodeCluster();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ClusterClient clusterClient = new ClusterClient();
        Gateway gateway = new Gateway(clusterClient);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String gatewayUrl = "http://localhost:3000/placeOrder";
        int numberOfCalls = 1;
        ApiRequest apiRequest = new ApiRequest(gatewayUrl, numberOfCalls);
        apiRequest.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clusterClient.close();
        clusterService.close();

        gateway.Close();
    }

}
