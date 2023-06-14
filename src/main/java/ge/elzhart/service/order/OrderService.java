package ge.elzhart.service.order;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.NodeValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.api.mapper.OrderMapper;
import ge.elzhart.api.mapper.RecordSearchMapper;
import ge.elzhart.exception.NotFoundException;
import ge.elzhart.model.domain.Order;
import ge.elzhart.model.domain.OrderStatus;
import ge.elzhart.model.domain.SelectOrderProperties;
import ge.elzhart.model.domain.SelectType;
import ge.elzhart.model.domain.user.User;
import ge.elzhart.model.repository.OrderRepository;
import ge.elzhart.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;

    public List<OrderDto> findCreatedByUsername(String username) {
        List<Order> createdByUsername = orderRepository.findCreatedByUsername(username);
        return createdByUsername
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> findPostedByUsername(String username) {
        List<Order> postedByUsername = orderRepository.findPostedByUsername(username);
        return postedByUsername
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> findSelected(String username, SelectType selectType) {
        List<Order> selectedByUsername = orderRepository.findSelectedByUsername(username, selectType);
        return selectedByUsername
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<List<OrderDto>> findMatchedByUsername(String username) {
        return orderRepository.findMatchedByUsername(username, SelectType.FOR_MATCH).stream()
                .map(OrderService::getOrderDtos)
                .sorted(Comparator.comparing(List::size))
                .collect(Collectors.toList());
    }

    @NotNull
    private static List<OrderDto> getOrderDtos(ListValue listValue) {
        List<OrderDto> orderDtos = new ArrayList<>();
        Iterator<Value> iterator = listValue.values().iterator();
        while (iterator.hasNext()) {
            Value next = iterator.next();
            if (!iterator.hasNext() || !next.type().name().equals("NODE")) {
                continue;
            }
            NodeValue nodeValue = (NodeValue) next;

            if (!nodeValue.asNode().hasLabel("Order")) {
                continue;
            }
            orderDtos.add(RecordSearchMapper.fromRecordToOrder(nodeValue));
        }
        return orderDtos;
    }

    public List<OrderDto> create(String username, OrderDto orderDto) {
        User user = userService.findByUsername(username);

        Order order = orderMapper.toNode(orderDto)
                .withCreatedDate(LocalDateTime.now())
                .withCreatedBy(username);
        user.getCreated().add(order);
        User saved = userService.save(user);
        return saved.getCreated().stream().map(orderMapper::toDto).toList();
    }

    public List<OrderDto> selectForMatch(String username, List<UUID> orderIds) {
        User user = userService.findByUsername(username);

        List<String> ids = orderIds.stream().map(UUID::toString).toList();
        List<SelectOrderProperties> orderProperties = orderRepository.findAllById(ids)
                .stream()
                .map(order -> new SelectOrderProperties(SelectType.FOR_MATCH, order))
                .toList();
        user.getSelected().addAll(orderProperties);
        User saved = userService.save(user);
        return saved.getSelected().stream()
                .filter(properties -> properties.getType().equals(SelectType.FOR_MATCH))
                .map(properties -> orderMapper.toDto(properties.getOrder()))
                .toList();
    }

    public OrderDto update(String orderId, OrderDto orderDto, String username) {
        if (!orderDto.getStatus().equals(OrderStatus.DRAFT)) {
            throw new UnsupportedOperationException();
        }
        Order order = orderRepository.findById(orderId).orElseThrow(RuntimeException::new)
                .withCategory(orderDto.getCategory())
                .withCountry(orderDto.getCountry())
                .withTitle(orderDto.getTitle())
                .withLastModifiedDate(LocalDateTime.now())
                .withLastModifiedBy(username);
        Order updated = orderRepository.save(order);
        return orderMapper.toDto(updated);
    }

    public OrderDto changeStatus(String username, String orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(OrderDto.class, orderId))
                .withStatus(orderStatus)
                .withLastModifiedDate(LocalDateTime.now())
                .withLastModifiedBy(username);
        Order changed = orderRepository.save(order);
        return orderMapper.toDto(changed);
    }

    public void delete(String orderId) {
        orderRepository.findById(orderId).ifPresent(orderRepository::delete);
    }

    public void deleteAll(Set<String> orderIds) {
        orderRepository.deleteAll(orderRepository.findAllById(orderIds));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OrderDto> selectForTransaction(String username, UUID orderId) {
        User user = userService.findByUsername(username);
        user.getSelected().forEach(
                selected -> {
                    if (orderId.toString().equals(selected.getOrder().getId())) {
                        selected.setType(SelectType.FOR_TRANSACTION);
                    }
                }
        );
        User saved = userService.save(user);
        return saved.getSelected().stream()
                .filter(properties -> properties.getType().equals(SelectType.FOR_TRANSACTION))
                .map(properties -> orderMapper.toDto(properties.getOrder()))
                .toList();
    }
}
