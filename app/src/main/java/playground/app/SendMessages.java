package playground.app;

import io.aeron.cluster.client.AeronCluster;
import playground.app.OrderMessageEncoder;
import playground.app.MessageHeaderEncoder;

import java.util.UUID;

import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;

public class SendMessages {
      private final AeronCluster aeronCluster;
      private final OrderMessageEncoder OrderMessageEncoderCommand = new OrderMessageEncoder();
      private final MutableDirectBuffer sendBuffer = new ExpandableDirectByteBuffer(1024);
      private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();

      public SendMessages(AeronCluster aeronCluster) {
            this.aeronCluster = aeronCluster;
      }

      public String sendCustomMessage(long SystemOrderId, String Symbol, long Quantity) {
            final String correlationId = UUID.randomUUID().toString();
            OrderMessageEncoderCommand.wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder);
            OrderMessageEncoderCommand.uniqueClientOrderId(correlationId);
            OrderMessageEncoderCommand.systemOrderId(SystemOrderId);
            OrderMessageEncoderCommand.symbol(CryptoCurrencySymbol.valueOf(Symbol));
            OrderMessageEncoderCommand.quantity((long) Quantity);
            long offerResult = aeronCluster.offer(sendBuffer, 0, MessageHeaderEncoder.ENCODED_LENGTH +
                        OrderMessageEncoderCommand.encodedLength());

            if (offerResult >= 0L) {
                  return String.format(
                              "Unique client order ID: %s, System Order ID: %d, Symbol: %s, Quantity: %d",
                              correlationId, SystemOrderId, Symbol, Quantity);
            } else {
                  return "Failed to send message. Error code: " + offerResult;
            }
      }

}
