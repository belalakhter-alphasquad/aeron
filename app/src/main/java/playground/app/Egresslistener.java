package playground.app;

import org.agrona.DirectBuffer;

import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.logbuffer.Header;
import playground.app.HelloResponseBroadcastDecoder;
import playground.app.MessageHeaderDecoder;

public class Egresslistener implements EgressListener {
    private static final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private static final HelloResponseBroadcastDecoder helloResponseBroadcastDecoder = new HelloResponseBroadcastDecoder();

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
            case HelloResponseBroadcastDecoder.TEMPLATE_ID -> displayHelloResponseBroadcast(buffer, offset);
            default -> System.out.println("unknown message type: " + messageHeaderDecoder.templateId());
        }
    }

    private void displayHelloResponseBroadcast(final DirectBuffer buffer, final int offset) {
        helloResponseBroadcastDecoder.wrapAndApplyHeader(buffer, offset, messageHeaderDecoder);

        final String clientName = helloResponseBroadcastDecoder.clientName();
        final String echoMessage = helloResponseBroadcastDecoder.clientMesssageEcho();

        System.out.println("Client Name: " + clientName + ", Client Message: " + echoMessage);
        System.out.println("/n");
        System.out.println("This is from Egress listener\n");
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
