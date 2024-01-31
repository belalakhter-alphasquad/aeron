package playground.app;

import org.agrona.DirectBuffer;
import playground.app.MessageHeaderDecoder;
import playground.app.OrderMessageDecoder;

import io.aeron.cluster.service.ClientSession;

public class SbeDemuxer {
    private static final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private static final OrderMessageDecoder orderMessageDecoder = new OrderMessageDecoder();

    public static void dispatch(final ClientSession session, final DirectBuffer buffer, final int offset,
            final int length) {

        headerDecoder.wrap(buffer, offset);

        switch (headerDecoder.templateId()) {

            case OrderMessageDecoder.TEMPLATE_ID -> {
                OnOrderMessage(buffer, offset);
            }

            default -> System.out.println("Unknown message received from gateway");
        }
    }

    private static void OnOrderMessage(DirectBuffer buffer, int offset) {
        orderMessageDecoder.wrapAndApplyHeader(buffer, offset, headerDecoder);
        System.out.println("unique client order ID: " + orderMessageDecoder.uniqueClientOrderId() + " System Order ID: "
                + orderMessageDecoder.systemOrderId() + " Symbol: " + orderMessageDecoder.symbol()
                + " received on cluster service\n");
    }
}
