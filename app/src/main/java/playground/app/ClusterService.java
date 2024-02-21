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
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.CloseHelper;
import java.io.File;
import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.ErrorHandler;
import org.agrona.ExpandableArrayBuffer;
import playground.app.DemoResponseEncoder;
import playground.app.OrderBook;
import playground.app.MessageHeaderEncoder;
import playground.app.OrderMessageDecoder;
import playground.app.OrderMessageEncoder;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.agrona.concurrent.status.AtomicCounter;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;

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

    private final static MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final static DemoResponseEncoder DemoResponseEncoderCommand = new DemoResponseEncoder();
    private static final OrderMessageDecoder orderMessageDecoder = new OrderMessageDecoder();
    private final static MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final static OrderMessageEncoder OrderMessageEncoderCommand = new OrderMessageEncoder();
    static int clusterServiceCount = 0;

    static class Service implements ClusteredService {
        protected Cluster cluster;
        protected IdleStrategy idleStrategy;
        private int messageCount = 0;
        private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(2048 * 2048);

        @Override
        public void onSessionOpen(final ClientSession session, final long timestamp) {
            allSessions.add(session);
        }

        /* */
        @Override
        public void onSessionMessage(
                final ClientSession session,
                final long timestamp,
                final DirectBuffer buffer,
                final int offset,
                final int length,
                final Header header) {

            messageCount++;

            messageHeaderDecoder.wrap(buffer, offset);
            switch (messageHeaderDecoder.templateId()) {

                case OrderMessageDecoder.TEMPLATE_ID -> {
                    orderMessageDecoder.wrapAndApplyHeader(buffer, offset, messageHeaderDecoder);
                    String uniqueId = orderMessageDecoder.uniqueClientOrderId();
                    Long OrderId = orderMessageDecoder.systemOrderId();
                    CryptoCurrencySymbol asset = orderMessageDecoder.symbol();
                    String symbol = asset.toString();
                    Long size = orderMessageDecoder.quantity();
                    final UnsafeBuffer sendBuffer = new UnsafeBuffer(ByteBuffer.allocate(1024 * 1024 * 64));
                    OrderMessageEncoderCommand.wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder);
                    OrderMessageEncoderCommand.uniqueClientOrderId(uniqueId);
                    OrderMessageEncoderCommand.systemOrderId(OrderId);
                    OrderMessageEncoderCommand.symbol(CryptoCurrencySymbol.valueOf(symbol));
                    OrderMessageEncoderCommand.quantity(size);
                    echoMessage(session, sendBuffer, 0,
                            messageHeaderEncoder.encodedLength() + OrderMessageEncoderCommand.encodedLength());

                    System.out.println("message recieved on cluster service" + uniqueId);

                }

                default -> System.out.println("Unknown message received from gateway");
            }
            final int id = buffer.getInt(offset);
            if (TIMER_ID == id) {
                idleStrategy.reset();
                while (!cluster.scheduleTimer(serviceCorrelationId(1), cluster.time() + 1_000)) {
                    idleStrategy.idle();
                }
            }

        }

        public void onTimerEvent(final long correlationId, final long timestamp) {
            System.out.println("onTimerEvent " + correlationId + "\n");

            final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
            buffer.putInt(0, 1);

            cluster.forEachClientSession((clientSession) -> echoMessage(clientSession, buffer, 0, SIZE_OF_INT));
        }

        public void onTakeSnapshot(final ExclusivePublication snapshotPublication) {
            System.out.println("onTakeSnapshot messageCount=" + messageCount + "\n");

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

        }

        protected long serviceCorrelationId(final int correlationId) {
            return ((long) cluster.context().serviceId()) << 56 | correlationId;
        }

        public void echoMessage(
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

        }

        public void onTerminate(final Cluster cluster) {
        }

        @Override
        public void onStart(final Cluster cluster, final Image snapshotImage) {
            this.cluster = cluster;
            this.idleStrategy = cluster.idleStrategy();
            System.out.println("Cluster Service is started");

            if (null != snapshotImage) {
                System.out.println("onStart load snapshot\n");
                final FragmentHandler fragmentHandler = (buffer, offset, length,
                        header) -> messageCount = buffer.getInt(offset);

                idleStrategy.reset();
                while (snapshotImage.poll(fragmentHandler, 1) <= 0) {
                    idleStrategy.idle();
                }

                System.out.println("snapshot messageCount=" + messageCount + "\n");
            }
        }

        @Override
        public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {
            System.out.println("Client has closed connection " + session.id() + " " + closeReason + "\n");
            allSessions.remove(session);
        }

    }

    public void RunClusterNode(int Node) {
        List<String> Single = List.of("localhost");
        List<String> ipAddresses = List.of("localhost");
        try {
            File baseDir = getBaseDir(Node);
            if (baseDir.exists()) {
                deleteDirectory(baseDir);
            }
            config = ClusterConfig.create(Node, Single,
                    Single, PORT_BASE, new Service());

            config.mediaDriverContext().dirDeleteOnStart(true);
            config.archiveContext().deleteArchiveOnStart(true);
            config.consensusModuleContext().ingressChannel("aeron:udp");
            config.baseDir(baseDir);
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

    private static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    @Override
    public void close() {
        if (clusteredMediaDriver != null) {
            System.out.println("CLosing cluster service\n");
            final ErrorHandler errorHandler = clusteredMediaDriver.mediaDriver().context().errorHandler();
            CloseHelper.close(errorHandler, clusteredMediaDriver.consensusModule());
            CloseHelper.close(errorHandler, container);
            CloseHelper.close(clusteredMediaDriver);
            // I m still not sure if its closed :)
            clusteredMediaDriver.close();
            container.close();
        } else {
            System.out.println("Service not running");
            return;
        }

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