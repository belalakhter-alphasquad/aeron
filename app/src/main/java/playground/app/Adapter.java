package playground.app;

import org.agrona.DirectBuffer;

import io.aeron.cluster.service.ClientSession;
import io.aeron.logbuffer.FragmentHandler;
import playground.app.OrderMessageDecoder;
import io.aeron.logbuffer.Header;
import io.aeron.logbuffer.FragmentHandler;
import playground.app.MessageHeaderDecoder;

public class Adapter implements FragmentHandler {
    private ClientSession session;

    public void setSession(final ClientSession session) {
        this.session = session;
    }

    @Override
    public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header) {
        messageHeaderDecoder.wrap(buffer, offset);
        final int templateId = messageHeaderDecoder.templateId();
        switch (templateId) {
            case OrderMessageDecoder.TEMPLATE_ID -> {
                orderMessageDecoder.wrapAndApplyHeader(buffer, offset, headerDecoder);
                System.out.println(
                        "unique client order ID: " + orderMessageDecoder.uniqueClientOrderId() + " System Order ID: "
                                + orderMessageDecoder.systemOrderId() + " Symbol: " + orderMessageDecoder.symbol()
                                + " received on cluster service\n");
            }

            default -> logger.error("Unknown message {}", templateId);
        }
    }

}
