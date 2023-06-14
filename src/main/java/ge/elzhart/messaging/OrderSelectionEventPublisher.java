package ge.elzhart.messaging;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSelectionEventPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(final String username, final String orderId) {
        OrderSelectionEvent customSpringEvent = new OrderSelectionEvent(this, username, orderId);
        log.info("Publishing order selection event. {}", customSpringEvent);
        applicationEventPublisher.publishEvent(customSpringEvent);
    }
}