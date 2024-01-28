package playground.app;

import org.agrona.DirectBuffer;

import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.logbuffer.Header;
import playground.app.HelloResponseBroadcastDecoder;
import playground.app.MessageHeaderDecoder;

public class Egresslistener implements EgressListener {
    private static final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private static final ClientSaysHelloDecoder clientSaysHelloDecoder = new ClientSaysHelloDecoder();

    public void EgressListener() {
        System.out.println("Egress listener constructor!");
    }

    @Override
    public void onMessage(
            final long clusterSessionId,
            final long timestamp,
            final DirectBuffer buffer,
            final int offset,
            final int length,
            final Header header) {
        if (length < MessageHeaderDecoder.ENCODED_LENGTH) {
            System.out.println("Message too short");
            return;
        }
        messageHeaderDecoder.wrap(buffer, offset);

        switch (messageHeaderDecoder.templateId()) {
            case ClientSaysHelloDecoder.TEMPLATE_ID -> displayClientSaysHelloDecoder(buffer, offset);
            default -> System.out.println("unknown message type: " + messageHeaderDecoder.templateId());
        }
    }

    private void displayClientSaysHelloDecoder(final DirectBuffer buffer, final int offset) {
        clientSaysHelloDecoder.wrapAndApplyHeader(buffer, offset, messageHeaderDecoder);

        final String clientName = clientSaysHelloDecoder.clientName();
        final String echoMessage = clientSaysHelloDecoder.clientMesssage();

        System.out.println(
                "\nClient Name: " + clientName + ", Client Message: " + echoMessage
                        + " -This is from Egress Listener\n");
    }

    @Override
    public void onSessionEvent(
            final long correlationId,
            final long clusterSessionId,
            final long leadershipTermId,
            final int leaderMemberId,
            final EventCode code,
            final String detail) {
        if (code != EventCode.OK) {
            System.out
                    .println("Session event: " + code.name() + " " + detail + ". leadershipTermId=" + leadershipTermId);
        }
    }

    @Override
    public void onNewLeader(
            final long clusterSessionId,
            final long leadershipTermId,
            final int leaderMemberId,
            final String ingressEndpoints) {
        System.out.println("New Leader: " + leaderMemberId + ". leadershipTermId=" + leadershipTermId);
    }

}
