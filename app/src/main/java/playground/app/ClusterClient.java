package playground.app;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import java.util.Arrays;
import java.util.List;

public class ClusterClient implements AutoCloseable {

    private static final long HEARTBEAT_INTERVAL_MS = 250;
    private long lastHeartbeatTime = 0;

    private final MediaDriver mediaDriver;
    private final AeronCluster aeronCluster;

    public ClusterClient() {
        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true));

        final String[] hostnames = System.getProperty(
                "aeron.cluster.tutorial.hostnames", "localhost,localhost,localhost").split(",");
        final String ingressEndpoints = ingressEndpoints(Arrays.asList(hostnames));

        aeronCluster = AeronCluster.connect(
                new AeronCluster.Context()
                        .egressChannel("aeron:udp?endpoint=localhost:0")
                        .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                        .ingressChannel("aeron:udp")
                        .ingressEndpoints(ingressEndpoints));
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
        }
    }

    @Override
    public void close() {
        aeronCluster.close();
        mediaDriver.close();
    }

    private String ingressEndpoints(final List<String> hostnames) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++) {
            sb.append(i).append('=');
            sb.append(hostnames.get(i)).append(':').append(
                    calculatePort(i, 9000));
            sb.append(',');
        }

        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    private int calculatePort(final int nodeId, final int basePort) {
        return basePort + nodeId;
    }
}
