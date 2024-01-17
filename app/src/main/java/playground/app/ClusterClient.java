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
    private long lastHeartbeatTime = 0;

    private final MediaDriver mediaDriver;
    private final AeronCluster aeronCluster;

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

        aeronCluster = AeronCluster.connect(
                new AeronCluster.Context()
                        .egressChannel("aeron:udp?endpoint=" + userhost + ":" + port)
                        .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                        .ingressChannel("aeron:udp?term-length=64k")
                        .ingressEndpoints(ingressEndpoints));
        System.out.println("Cluster connection succeeded!" + " Leader is node " + aeronCluster.leaderMemberId() + "\n");

    }

    public AeronCluster getAeronCluster() {

        return aeronCluster;
    }

    public void sendKeepAlive() {
        final long now = System.currentTimeMillis();
        if (now >= (lastHeartbeatTime + HEARTBEAT_INTERVAL_MS)) {
            lastHeartbeatTime = now;
            if (aeronCluster.isClosed()) {
                return;
            }
            aeronCluster.sendKeepAlive();
            if (null != aeronCluster && !aeronCluster.isClosed()) {
                aeronCluster.pollEgress();
            }
        }
    }

    @Override
    public void close() {
        aeronCluster.close();
        mediaDriver.close();
    }

}
