package ge.elzhart.integration.data;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.model.domain.OrderCategory;
import ge.elzhart.model.domain.OrderStatus;
import ge.elzhart.model.domain.SelectType;
import ge.elzhart.service.order.OrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderTestDataFactory {

    private final OrderService orderService;

    public OrderDto createOrder(
            String ownerName,
            String title,
            OrderCategory category,
            OrderStatus status,
            String country) {

        return orderService.create(
                ownerName,
                new OrderDto()
                        .setTitle(title)
                        .setCategory(category)
                        .setStatus(status)
                        .setCountry(country)
        ).get(0);
    }

    public OrderDto selectOrder(String ownerName, String orderId, SelectType selectType) {

        switch (selectType) {
            case FOR_MATCH -> {
                return orderService.selectForMatch(
                        ownerName,
                        List.of(UUID.fromString(orderId))).get(0);
            }
            case FOR_TRANSACTION -> {
                return orderService.selectForTransaction(ownerName, UUID.fromString(orderId)).get(0);
            }
        }
        return null;
    }

    public void deleteAll(Set<String> orderIds) {
        orderService.deleteAll(orderIds);
    }
}
