package ge.elzhart.integration.data;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.model.domain.order.OrderCategory;
import ge.elzhart.model.domain.order.OrderStatus;
import ge.elzhart.model.domain.user.SelectType;
import ge.elzhart.service.order.OrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderTestDataFactory {

    private final OrderService orderService;

    public OrderDto createOrder(
            String username,
            String title,
            OrderCategory category,
            OrderStatus status,
            String country) {

        return orderService.create(
                username,
                new OrderDto()
                        .setTitle(title)
                        .setCategory(category)
                        .setStatus(status)
                        .setCountry(country)
        ).get(0);
    }

    public OrderDto selectOrder(String username, String orderId, SelectType selectType) {

        switch (selectType) {
            case FOR_MATCH -> {
                return orderService.selectForMatch(
                        username,
                        List.of(UUID.fromString(orderId))).get(0);
            }
            case FOR_TRANSACTION -> {
                return orderService.selectForTransaction(username, UUID.fromString(orderId)).get(0);
            }
        }
        return null;
    }

    public void deleteAll(Set<String> orderIds) {
        orderService.deleteAll(orderIds);
    }
}
