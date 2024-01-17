package playground.app;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.samples.cluster.ClusterConfig;
import playground.app.utils.Enviromental;

import java.util.Arrays;
import java.util.List;

public class ClusterClient implements AutoCloseable {

    private static final long HEARTBEAT_INTERVAL_MS = 250;

    private final MediaDriver mediaDriver;
    private Egresslistener clientEgresslistener;
    private final AeronCluster aeronCluster;
    private Thread pollingThread;
    private volatile boolean running = true;

    public ClusterClient() {

        final int port = Enviromental.tryGetResponsePortFromEnv();
        final String userhost = Enviromental.getThisHostName();

        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true));

        final List<String> hostnames = Arrays.asList(Enviromental.tryGetClusterHostsFromEnv().split(","));
        final String ingressEndpoints = ClusterConfig.ingressEndpoints(
                hostnames, 9000, ClusterConfig.CLIENT_FACING_PORT_OFFSET);

        clientEgresslistener = new Egresslistener();

        aeronCluster = AeronCluster.connect(
                new AeronCluster.Context()
                        .egressListener(clientEgresslistener)
                        .egressChannel("aeron:udp?endpoint=" + userhost + ":" + port)
                        .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                        .ingressChannel("aeron:udp?term-length=64k")
                        .ingressEndpoints(ingressEndpoints));
        System.out.println("Cluster connection succeeded!" + " Leader is node " + aeronCluster.leaderMemberId() + "\n");
    }

    public AeronCluster getAeronCluster() {

        return aeronCluster;
    }

    public void makeClusterAlive() {
        pollingThread = new Thread(() -> {
            while (running && !aeronCluster.isClosed()) {
                aeronCluster.sendKeepAlive();
                System.out.println("Sending Heartbeat to the Cluster!");
                aeronCluster.pollEgress();
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Cluster connection is closed \n");
                }
            }
        });

        pollingThread.start();
    }

    @Override
    public void close() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
            try {
                pollingThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted closing the connection \n");
            }
        }
        aeronCluster.close();
        mediaDriver.close();
    }

}
