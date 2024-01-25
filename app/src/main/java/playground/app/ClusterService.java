package playground.app;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.ClusterControl;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import io.aeron.samples.cluster.ClusterConfig;
import org.agrona.CloseHelper;
import java.io.File;
import org.agrona.DirectBuffer;
import org.agrona.ErrorHandler;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.agrona.concurrent.status.AtomicCounter;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import static org.agrona.BitUtil.SIZE_OF_INT;

public final class ClusterService implements AutoCloseable {

    private static final int SEND_ATTEMPTS = 3;
    private static final int TIMER_ID = 2;
    private static final int PORT_BASE = 9000;
    private final static List<ClientSession> allSessions = new ArrayList<>();

    final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy();
    private ClusterConfig config;
    private ClusteredMediaDriver clusteredMediaDriver;
    private ClusteredServiceContainer container;

    static class Service implements ClusteredService {
        protected Cluster cluster;
        protected IdleStrategy idleStrategy;
        private int messageCount = 0;
        private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();

        @Override
        public void onSessionOpen(final ClientSession session, final long timestamp) {
            System.out.println("onSessionOpen " + session.id());
            allSessions.add(session);
        }

        @Override
        public void onSessionMessage(
                final ClientSession session,
                final long timestamp,
                final DirectBuffer buffer,
                final int offset,
                final int length,
                final Header header) {
            messageCount++;
            System.out.println(cluster.role() + " onSessionMessage " + session.id() + " count=" + messageCount);

            final int id = buffer.getInt(offset);
            if (TIMER_ID == id) {
                idleStrategy.reset();
                while (!cluster.scheduleTimer(serviceCorrelationId(1), cluster.time() + 1_000)) {
                    idleStrategy.idle();
                }
            } else {
                echoMessage(session, buffer, offset, length);
            }
        }

        public void onTimerEvent(final long correlationId, final long timestamp) {
            System.out.println("onTimerEvent " + correlationId);

            final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
            buffer.putInt(0, 1);

            cluster.forEachClientSession((clientSession) -> echoMessage(clientSession, buffer, 0, SIZE_OF_INT));
        }

        public void onTakeSnapshot(final ExclusivePublication snapshotPublication) {
            System.out.println("onTakeSnapshot messageCount=" + messageCount);

            buffer.putInt(0, messageCount);
            idleStrategy.reset();
            while (snapshotPublication.offer(buffer, 0, 4) < 0) {
                idleStrategy.idle();
            }
        }

        public void onNewLeadershipTermEvent(
                final long leadershipTermId,
                final long logPosition,
                final long timestamp,
                final long termBaseLogPosition,
                final int leaderMemberId,
                final int logSessionId,
                final TimeUnit timeUnit,
                final int appVersion) {
            System.out.println("onNewLeadershipTermEvent");
        }

        protected long serviceCorrelationId(final int correlationId) {
            return ((long) cluster.context().serviceId()) << 56 | correlationId;
        }

        private void echoMessage(
                final ClientSession session, final DirectBuffer buffer, final int offset, final int length) {
            idleStrategy.reset();
            int attempts = SEND_ATTEMPTS;
            do {
                final long result = session.offer(buffer, offset, length);
                if (result > 0) {
                    return;
                }
                idleStrategy.idle();
            } while (--attempts > 0);
        }

        public void onRoleChange(final Cluster.Role newRole) {
            System.out.println("onRoleChange " + newRole);
        }

        public void onTerminate(final Cluster cluster) {
        }

        @Override
        public void onStart(final Cluster cluster, final Image snapshotImage) {
            this.cluster = cluster;
            this.idleStrategy = cluster.idleStrategy();

            if (null != snapshotImage) {
                System.out.println("onStart load snapshot");
                final FragmentHandler fragmentHandler = (buffer, offset, length,
                        header) -> messageCount = buffer.getInt(offset);

                idleStrategy.reset();
                while (snapshotImage.poll(fragmentHandler, 1) <= 0) {
                    idleStrategy.idle();
                }

                System.out.println("snapshot messageCount=" + messageCount);
            }
        }

        @Override
        public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {
            System.out.println("onSessionClose " + session.id() + " " + closeReason);
            allSessions.remove(session);
        }

    }

    public void SingleNodeCluster() {
        try {

            config = ClusterConfig.create(0, Collections.singletonList("127.0.0.1"), PORT_BASE, new Service());

            config.mediaDriverContext().dirDeleteOnStart(true);
            config.archiveContext().deleteArchiveOnStart(true);
            config.consensusModuleContext().ingressChannel("aeron:udp");
            config.baseDir(getBaseDir(0));
            config.consensusModuleContext().leaderHeartbeatTimeoutNs(TimeUnit.SECONDS.toNanos(3));

            clusteredMediaDriver = ClusteredMediaDriver.launch(
                    config.mediaDriverContext(),
                    config.archiveContext(),
                    config.consensusModuleContext());

            container = ClusteredServiceContainer.launch(config.clusteredServiceContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static File getBaseDir(final int nodeId) {
        final String baseDir = System.getenv("BASE_DIR");
        if (null == baseDir || baseDir.isEmpty()) {
            return new File(System.getProperty("user.dir"), "node" + nodeId);
        }

        return new File(baseDir);
    }

    public void close() {
        final ErrorHandler errorHandler = clusteredMediaDriver.mediaDriver().context().errorHandler();
        CloseHelper.close(errorHandler, clusteredMediaDriver.consensusModule());
        CloseHelper.close(errorHandler, container);
        CloseHelper.close(clusteredMediaDriver);
    }

    void takeSnapshot() {
        final ConsensusModule.Context consensusModuleContext = clusteredMediaDriver.consensusModule().context();
        final AtomicCounter snapshotCounter = consensusModuleContext.snapshotCounter();
        final long snapshotCount = snapshotCounter.get();

        final AtomicCounter controlToggle = ClusterControl.findControlToggle(
                clusteredMediaDriver.mediaDriver().context().countersManager(),
                consensusModuleContext.clusterId());
        ClusterControl.ToggleState.SNAPSHOT.toggle(controlToggle);

        idleStrategy.reset();
        while (snapshotCounter.get() <= snapshotCount) {
            idleStrategy.idle();
        }
    }

}
