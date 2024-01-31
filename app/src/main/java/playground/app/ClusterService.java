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
import org.agrona.DirectBuffer;
import playground.app.MessageHeaderDecoder;
import org.agrona.CloseHelper;
import java.io.File;

import org.agrona.DirectBuffer;
import org.agrona.ErrorHandler;
import org.agrona.ExpandableArrayBuffer;
import playground.app.DemoResponseEncoder;
import playground.app.MessageHeaderEncoder;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.agrona.concurrent.status.AtomicCounter;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import static org.agrona.BitUtil.SIZE_OF_INT;

public final class ClusterService implements ClusteredService {
    private final Adapter Adapter;

    @Override
    public void onSessionMessage(
            final ClientSession session,
            final long timestamp,
            final DirectBuffer buffer,
            final int offset,
            final int length,
            final Header header) {
        Adapter.setSession(session);
        Adapter.onFragment(buffer, offset, length, header);
    }

    /*
     * private static final int SEND_ATTEMPTS = 3;
     * private static final int TIMER_ID = 2;
     * private static final int PORT_BASE = 9000;
     * private final static List<ClientSession> allSessions = new ArrayList<>();
     * 
     * final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy();
     * private ClusterConfig config;
     * private ClusteredMediaDriver clusteredMediaDriver;
     * private ClusteredServiceContainer container;
     * 
     * private final static MessageHeaderEncoder messageHeaderEncoder = new
     * MessageHeaderEncoder();
     * private final static DemoResponseEncoder DemoResponseEncoderCommand = new
     * DemoResponseEncoder();
     * 
     * static class Service implements ClusteredService {
     * protected Cluster cluster;
     * protected IdleStrategy idleStrategy;
     * private int messageCount = 0;
     * private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
     * 
     * @Override
     * public void onSessionMessage(
     * final ClientSession session,
     * final long timestamp,
     * final DirectBuffer buffer,
     * final int offset,
     * final int length,
     * final Header header) {
     * 
     * }
     * 
     * @Override
     * public void onSessionOpen(final ClientSession session, final long timestamp)
     * {
     * allSessions.add(session);
     * }
     * 
     * public void onTimerEvent(final long correlationId, final long timestamp) {
     * System.out.println("onTimerEvent " + correlationId + "\n");
     * 
     * final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
     * buffer.putInt(0, 1);
     * 
     * cluster.forEachClientSession((clientSession) -> echoMessage(clientSession,
     * buffer, 0, SIZE_OF_INT));
     * }
     * 
     * public void onTakeSnapshot(final ExclusivePublication snapshotPublication) {
     * System.out.println("onTakeSnapshot messageCount=" + messageCount + "\n");
     * 
     * buffer.putInt(0, messageCount);
     * idleStrategy.reset();
     * while (snapshotPublication.offer(buffer, 0, 4) < 0) {
     * idleStrategy.idle();
     * }
     * }
     * 
     * public void onNewLeadershipTermEvent(
     * final long leadershipTermId,
     * final long logPosition,
     * final long timestamp,
     * final long termBaseLogPosition,
     * final int leaderMemberId,
     * final int logSessionId,
     * final TimeUnit timeUnit,
     * final int appVersion) {
     * 
     * }
     * 
     * protected long serviceCorrelationId(final int correlationId) {
     * return ((long) cluster.context().serviceId()) << 56 | correlationId;
     * }
     * 
     * public void echoMessage(
     * final ClientSession session, final DirectBuffer buffer, final int offset,
     * final int length) {
     * idleStrategy.reset();
     * int attempts = SEND_ATTEMPTS;
     * do {
     * final long result = session.offer(buffer, offset, length);
     * if (result > 0) {
     * return;
     * }
     * idleStrategy.idle();
     * } while (--attempts > 0);
     * }
     * 
     * public void onRoleChange(final Cluster.Role newRole) {
     * 
     * }
     * 
     * public void onTerminate(final Cluster cluster) {
     * }
     * 
     * @Override
     * public void onStart(final Cluster cluster, final Image snapshotImage) {
     * this.cluster = cluster;
     * this.idleStrategy = cluster.idleStrategy();
     * System.out.println("Cluster Service is started");
     * 
     * if (null != snapshotImage) {
     * System.out.println("onStart load snapshot\n");
     * final FragmentHandler fragmentHandler = (buffer, offset, length,
     * header) -> messageCount = buffer.getInt(offset);
     * 
     * idleStrategy.reset();
     * while (snapshotImage.poll(fragmentHandler, 1) <= 0) {
     * idleStrategy.idle();
     * }
     * 
     * System.out.println("snapshot messageCount=" + messageCount + "\n");
     * }
     * }
     * 
     * @Override
     * public void onSessionClose(ClientSession session, long timestamp, CloseReason
     * closeReason) {
     * System.out.println("Client has closed connection " + session.id() + " " +
     * closeReason + "\n");
     * allSessions.remove(session);
     * }
     * 
     * }
     * 
     * public void OnFragment(final DirectBuffer buffer, final int offset, final int
     * length, final Header header) {
     * 
     * }
     * 
     * public void RunClusterNode(int Node) {
     * List<String> Single = List.of("192.168.18.18");
     * List<String> ipAddresses = List.of("192.168.18.18", "192.168.18.19",
     * "192.168.18.20");
     * try {
     * File baseDir = getBaseDir(Node);
     * if (baseDir.exists()) {
     * deleteDirectory(baseDir);
     * }
     * config = ClusterConfig.create(Node, Single,
     * Single, PORT_BASE, new Service());
     * 
     * config.mediaDriverContext().dirDeleteOnStart(true);
     * config.archiveContext().deleteArchiveOnStart(true);
     * config.consensusModuleContext().ingressChannel("aeron:udp");
     * config.baseDir(baseDir);
     * config.consensusModuleContext().leaderHeartbeatTimeoutNs(TimeUnit.SECONDS.
     * toNanos(3));
     * 
     * clusteredMediaDriver = ClusteredMediaDriver.launch(
     * config.mediaDriverContext(),
     * config.archiveContext(),
     * config.consensusModuleContext());
     * 
     * container =
     * ClusteredServiceContainer.launch(config.clusteredServiceContext());
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * 
     * }
     * 
     * private static File getBaseDir(final int nodeId) {
     * final String baseDir = System.getenv("BASE_DIR");
     * if (null == baseDir || baseDir.isEmpty()) {
     * return new File(System.getProperty("user.dir"), "node" + nodeId);
     * }
     * 
     * return new File(baseDir);
     * }
     * 
     * private static void deleteDirectory(File directory) {
     * if (directory.isDirectory()) {
     * File[] files = directory.listFiles();
     * if (files != null) {
     * for (File file : files) {
     * deleteDirectory(file);
     * }
     * }
     * }
     * directory.delete();
     * }
     * 
     * @Override
     * public void close() {
     * if (clusteredMediaDriver != null) {
     * System.out.println("CLosing cluster service\n");
     * final ErrorHandler errorHandler =
     * clusteredMediaDriver.mediaDriver().context().errorHandler();
     * CloseHelper.close(errorHandler, clusteredMediaDriver.consensusModule());
     * CloseHelper.close(errorHandler, container);
     * CloseHelper.close(clusteredMediaDriver);
     * // I m still not sure if its closed :)
     * clusteredMediaDriver.close();
     * container.close();
     * } else {
     * System.out.println("Service not running");
     * return;
     * }
     * 
     * }
     * 
     * void takeSnapshot() {
     * final ConsensusModule.Context consensusModuleContext =
     * clusteredMediaDriver.consensusModule().context();
     * final AtomicCounter snapshotCounter =
     * consensusModuleContext.snapshotCounter();
     * final long snapshotCount = snapshotCounter.get();
     * 
     * final AtomicCounter controlToggle = ClusterControl.findControlToggle(
     * clusteredMediaDriver.mediaDriver().context().countersManager(),
     * consensusModuleContext.clusterId());
     * ClusterControl.ToggleState.SNAPSHOT.toggle(controlToggle);
     * 
     * idleStrategy.reset();
     * while (snapshotCounter.get() <= snapshotCount) {
     * idleStrategy.idle();
     * }
     * }
     */
}