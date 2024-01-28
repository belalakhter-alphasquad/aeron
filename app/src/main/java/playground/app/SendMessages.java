package playground.app;

import io.aeron.cluster.client.AeronCluster;
import playground.app.ClientSaysHelloEncoder;
import playground.app.MessageHeaderEncoder;

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
                  return "Message sent successfully. " + correlationId;
            } else {
                  return "Failed to send message. Error code: " + offerResult;
            }
      }

}
