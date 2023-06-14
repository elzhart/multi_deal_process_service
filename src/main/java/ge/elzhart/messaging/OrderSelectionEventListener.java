package ge.elzhart.messaging;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import ge.elzhart.api.dto.transaction.TransactionEventDto;
import ge.elzhart.api.dto.transaction.TransactionGraphDto;
import ge.elzhart.service.NotificationService;
import ge.elzhart.service.transaction.TransactionGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderSelectionEventListener implements ApplicationListener<OrderSelectionEvent> {

    private final TransactionGraphService transactionGraphService;
    private final NotificationService notificationService;

    @Override
    public void onApplicationEvent(OrderSelectionEvent event) {
        log.info("Received spring custom event - {}", event);
        List<TransactionGraphDto> transactionGraph = transactionGraphService.findTransactionGraph(event.getUsername(), event.getOrderId());
        if (!transactionGraph.isEmpty()) {
            transactionGraphService.transactionOrders(transactionGraph);
            transactionGraph.forEach(transaction -> notificationService.sendNotification(
                    transaction.getUsername(),
                    new TransactionEventDto(
                            "TRANSACTION_COMPLETE",
                            Map.of("transaction order", transaction.getOrderTitle())))
            );
        }
    }
}