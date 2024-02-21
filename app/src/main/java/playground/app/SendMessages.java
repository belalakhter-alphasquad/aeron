package playground.app;

import io.aeron.cluster.client.AeronCluster;
import playground.app.OrderMessageEncoder;
import playground.app.MessageHeaderEncoder;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class SendMessages {
      int clusterClientCount = 0;
      private final AeronCluster aeronCluster;
      private final OrderMessageEncoder OrderMessageEncoderCommand = new OrderMessageEncoder();
      private UnsafeBuffer sendBuffer = new UnsafeBuffer(ByteBuffer.allocate(1024 * 1024 * 64));
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
                  return correlationId;

            } else {
                  return "Failed to send message. Error code: " + offerResult;
            }
      }

}
