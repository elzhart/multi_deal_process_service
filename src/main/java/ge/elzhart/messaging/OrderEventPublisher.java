package ge.elzhart.messaging;


public interface OrderEventPublisher {
    void publish(final String ownerName, final String orderId);
}
