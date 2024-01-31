package playground.app.utils;

import static java.lang.Integer.parseInt;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Enviromental {

    public static String getThisHostName() {
        try {
            final Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (interfaceEnumeration.hasMoreElements()) {
                final var networkInterface = interfaceEnumeration.nextElement();

                if (networkInterface.getName().startsWith("eth0")) {
                    final Enumeration<InetAddress> interfaceAddresses = networkInterface.getInetAddresses();
                    while (interfaceAddresses.hasMoreElements()) {
                        if (interfaceAddresses.nextElement() instanceof Inet4Address inet4Address) {
                            return inet4Address.getHostAddress();
                        }
                    }
                }
            }
        } catch (final Exception e) {
            // ignore
        }
        return "localhost";
    }

    public static String tryGetClusterHostsFromEnv() {
        String clusterAddresses = "localhost";
        /*
         * String clusterAddresses = System.getenv("CLUSTER_ADDRESSES");
         * if (null == clusterAddresses || clusterAddresses.isEmpty()) {
         * clusterAddresses = System.getProperty("cluster.addresses", "54.88.62.187");
         * }
         */
        return clusterAddresses;
    }

    public static int tryGetResponsePortFromEnv() {
        String responsePort = System.getenv("RESPONSE_PORT");
        if (null == responsePort || responsePort.isEmpty()) {
            responsePort = System.getProperty("response.port", "0");
        }

        return parseInt(responsePort);
    }

}
