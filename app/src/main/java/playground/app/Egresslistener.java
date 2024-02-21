package playground.app;

import org.agrona.DirectBuffer;

import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.logbuffer.Header;
import playground.app.DemoResponseEncoder;
import playground.app.OrderBook;
import playground.app.MessageHeaderEncoder;
import playground.app.OrderMessageDecoder;
import playground.app.OrderMessageEncoder;

public class Egresslistener implements EgressListener {

    private final static DemoResponseDecoder DemoResponseDecoderCommand = new DemoResponseDecoder();
    private final static MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final static DemoResponseEncoder DemoResponseEncoderCommand = new DemoResponseEncoder();
    private static final OrderMessageDecoder orderMessageDecoder = new OrderMessageDecoder();
    private final static MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final static OrderMessageEncoder OrderMessageEncoderCommand = new OrderMessageEncoder();
    int egressListenerCount = 0;

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
            case OrderMessageDecoder.TEMPLATE_ID -> demoResponseDecoderCommand(buffer, offset);
            default -> System.out.println("unknown message type: " + messageHeaderDecoder.templateId());
        }
    }

    private void demoResponseDecoderCommand(final DirectBuffer buffer, final int offset) {
        orderMessageDecoder.wrapAndApplyHeader(buffer, offset, messageHeaderDecoder);
        String uniqueId = orderMessageDecoder.uniqueClientOrderId();
        Long OrderId = orderMessageDecoder.systemOrderId();
        CryptoCurrencySymbol asset = orderMessageDecoder.symbol();
        String symbol = asset.toString();
        Long size = orderMessageDecoder.quantity();
        OrderBook.OrderDetails details = new OrderBook.OrderDetails(OrderId, symbol, size);
        OrderBook.storeOrder(uniqueId, details);

        System.out.println(
                "\n Quick Response Egress listerner recieved: " + uniqueId + "\n");
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
