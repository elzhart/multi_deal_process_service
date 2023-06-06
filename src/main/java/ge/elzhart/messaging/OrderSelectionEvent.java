package ge.elzhart.messaging;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrderSelectionEvent extends ApplicationEvent {
    private final String username;
    private final String orderId;

    public OrderSelectionEvent(Object source, String username, String orderId) {
        super(source);
        this.username = username;
        this.orderId = orderId;
    }
}
