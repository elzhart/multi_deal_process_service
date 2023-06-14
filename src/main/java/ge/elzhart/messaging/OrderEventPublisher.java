package ge.elzhart.messaging;


public interface OrderEventPublisher {
    void publish(final String username, final String orderId);
}
