package playground.app;

import java.util.HashMap;

public class OrderBook {
    private static final HashMap<String, OrderDetails> orderMap = new HashMap<>();

    public static void storeOrder(String uniqueId, OrderDetails details) {
        orderMap.put(uniqueId, details);
    }

    public static OrderDetails getOrder(String uniqueId) {
        return orderMap.remove(uniqueId);
    }

    public static class OrderDetails {
        private final Long orderId;
        private final String symbol;
        private final Long size;

        public OrderDetails(Long orderId, String symbol, Long size) {
            this.orderId = orderId;
            this.symbol = symbol;
            this.size = size;
        }

        public Long getOrderId() {
            return orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public Long getSize() {
            return size;
        }
    }
}
