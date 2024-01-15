package playground.app;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.samples.simple.SimpleMessageEncoder;
import io.aeron.samples.simple.MessageHeaderEncoder;

import org.agrona.BufferUtil;
import org.agrona.concurrent.UnsafeBuffer;

public class SendMessages {
      private final AeronCluster aeronCluster;

      public SendMessages(AeronCluster aeronCluster) {
            this.aeronCluster = aeronCluster;
      }

      public boolean sendCustomMessage() {
            UnsafeBuffer buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(25, 64));

            SimpleMessageEncoder messageEncoder = new SimpleMessageEncoder();
            MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
            String messageContent = "Y";
            messageEncoder.wrapAndApplyHeader(buffer, 0, headerEncoder)
                        .content(messageContent);

            long result = aeronCluster.offer(buffer, 0, messageEncoder.encodedLength());
            if (result >= 0) {
                  System.out.println("Message sent successfully");
                  return true;
            } else {
                  System.out.println("Failed to send message, result: " + result);
                  return false;
            }
      }
}
