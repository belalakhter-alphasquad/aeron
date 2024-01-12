package playground.app;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        
        System.out.println("Welcome to Bilal's Playground");
        connectCluster(9000, 9000, "localhost", "localhost");
    }
    private static void connectCluster(
        final int basePort,
        final int port,
        final String clusterHosts,
        final String localHostName) {

        final List<String> hostnames = Arrays.asList(clusterHosts.split(","));
        final String ingressEndpoints = generateIngressEndpoints(hostnames, basePort);
        final String egressChannel = "aeron:udp?endpoint=" + localHostName + ":" + port;

        System.out.println("\n Printing the ingressEndpoints ==>  ");
        System.out.println(ingressEndpoints);
        System.out.println("\n\n");

       
        try (MediaDriver mediaDriver = MediaDriver.launch(new MediaDriver.Context()
        .threadingMode(ThreadingMode.SHARED)
        .dirDeleteOnStart(true)
        .dirDeleteOnShutdown(true))) {

    try (AeronCluster aeronCluster = AeronCluster.connect(new AeronCluster.Context()
            // .egressChannel(egressChannel)
             .ingressChannel("aeron:udp?term-length=64k")
             .ingressEndpoints(ingressEndpoints)))
            // .aeronDirectoryName(mediaDriver.aeronDirectoryName()))) {
                {

        log("Connected to cluster leader, node " + aeronCluster.leaderMemberId());
                }
          }
        
        }
    

        private static String generateIngressEndpoints(List<String> hostnames, int basePort) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hostnames.size(); i++) {
                int port = basePort + i;
                if (i > 0) sb.append(",");
                //
                sb.append(hostnames.get(i)).append(":").append(port).append("=").append(5);
                //sb.append(hostnames.get(i)).append(":").append(port);
            }
            return sb.toString();
        }
        
    private static void log(String message) {
        System.out.println(message);
    }
}