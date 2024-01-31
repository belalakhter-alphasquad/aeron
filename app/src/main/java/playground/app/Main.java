package playground.app;

import org.jline.reader.UserInterruptException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public class Main {
    public static void main(final String[] args) {
        Boolean Signal = true;

        ClusterService clusterService = new ClusterService();
        clusterService.RunClusterNode();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ClusterClient clusterClient = new ClusterClient();
        Gateway gateway = new Gateway(clusterClient);

        LineReader Reader = LineReaderBuilder.builder().build();
        while (Signal) {
            try {
                Reader.readLine();
            } catch (UserInterruptException e) {
                System.out.println("Services are closing");
                clusterClient.close();
                clusterService.close();
                gateway.Close();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                Signal = false;
            }

        }
    }
}