package ge.elzhart.messaging;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import ge.elzhart.api.dto.TransactionEventDto;
import ge.elzhart.api.dto.TransactionGraphDto;
import ge.elzhart.service.NotificationService;
import ge.elzhart.service.OrderSearchService;
import ge.elzhart.service.OwnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderSelectionEventListener implements ApplicationListener<OrderSelectionEvent> {

    private final OrderSearchService orderSearchService;
    private final OwnerService ownerService;
    private final NotificationService notificationService;

    @Override
    public void onApplicationEvent(OrderSelectionEvent event) {
        log.info("Received spring custom event - {}", event);
        List<TransactionGraphDto> transactionGraph = orderSearchService.findTransactionGraph(event.getUsername(), event.getOrderId());
        if (!transactionGraph.isEmpty()) {
            ownerService.transactionOrders(transactionGraph);
            transactionGraph.forEach(transaction -> notificationService.sendNotification(
                    transaction.getOwnerName(),
                    new TransactionEventDto(
                            "TRANSACTION_COMPLETE",
                            Map.of("transaction order", transaction.getOrderTitle())))
            );
        }
    }
}