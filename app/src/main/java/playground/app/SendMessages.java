package playground.app;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.samples.simple.ClientSaysHelloEncoder;
import io.aeron.samples.simple.MessageHeaderEncoder;

import java.util.UUID;

import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;

public class SendMessages {
      private final AeronCluster aeronCluster;
      private final ClientSaysHelloEncoder clientSaysHelloCommandEncoder = new ClientSaysHelloEncoder();
      private final MutableDirectBuffer sendBuffer = new ExpandableDirectByteBuffer(1024);
      private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();

      public SendMessages(AeronCluster aeronCluster) {
            this.aeronCluster = aeronCluster;
      }

      public String sendCustomMessage() {
            final String correlationId = UUID.randomUUID().toString();

            clientSaysHelloCommandEncoder.wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder);
            clientSaysHelloCommandEncoder.correlationId(correlationId);
            clientSaysHelloCommandEncoder.clientName("Bilal");
            clientSaysHelloCommandEncoder.clientMesssage("Hello from Bilal");

            long offerResult = aeronCluster.offer(sendBuffer, 0, MessageHeaderEncoder.ENCODED_LENGTH +
                        clientSaysHelloCommandEncoder.encodedLength());

            if (offerResult >= 0L) {
                  return "Message sent successfully. Correlation ID: " + correlationId;
            } else {
                  return "Failed to send message. Error code: " + offerResult;
            }
      }

      /*
       * public boolean sendCustomMessage() {
       * UnsafeBuffer buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(25,
       * 64));
       * 
       * SimpleMessageEncoder messageEncoder = new SimpleMessageEncoder();
       * MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
       * String messageContent = "Y";
       * messageEncoder.wrapAndApplyHeader(buffer, 0, headerEncoder)
       * .content(messageContent);
       * 
       * long result = aeronCluster.offer(buffer, 0, messageEncoder.encodedLength());
       * if (result >= 0) {
       * System.out.println("Message sent successfully");
       * return true;
       * } else {
       * System.out.println("Failed to send message, result: " + result);
       * return false;
       * }
       * }
       */
}
