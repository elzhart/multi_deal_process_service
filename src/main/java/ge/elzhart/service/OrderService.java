package ge.elzhart.service;

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
import java.util.UUID;
import java.util.stream.Collectors;

import ge.elzhart.repository.OrderRepository;
import ge.elzhart.api.dto.OrderDto;
import ge.elzhart.api.mapper.OrderMapper;
import ge.elzhart.api.mapper.RecordSearchMapper;
import ge.elzhart.model.Order;
import ge.elzhart.model.OrderStatus;
import ge.elzhart.model.Owner;
import ge.elzhart.model.SelectOrderProperties;
import ge.elzhart.model.SelectType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OwnerService ownerService;

    public List<OrderDto> findCreatedByOwnerName(String name) {
        List<Order> postedByOwnerName = orderRepository.findCreatedByOwnerName(name);
        return postedByOwnerName
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> findPostedByOwnerName(String name) {
        List<Order> postedByOwnerName = orderRepository.findPostedByOwnerName(name);
        return postedByOwnerName
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> findSelected(String name, SelectType selectType) {
        List<Order> postedByOwnerName = orderRepository.findSelectedByOwnerName(name, selectType);
        return postedByOwnerName
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<List<OrderDto>> findMatchedByOwnerName(String name) {
        return orderRepository.findMatchedByOwnerName(name, SelectType.FOR_MATCH).stream()
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

    public List<OrderDto> create(String ownerName, OrderDto orderDto) {
        Owner owner = ownerService.findByName(ownerName);

        Order order = orderMapper.toNode(orderDto)
                .withCreatedDate(LocalDateTime.now())
                .withCreatedBy(ownerName);
        owner.getCreated().add(order);
        Owner saved = ownerService.save(owner);
        return saved.getCreated().stream().map(orderMapper::toDto).toList();
    }

    public List<OrderDto> selectForMatch(String ownerName, List<UUID> orderIds) {
        Owner owner = ownerService.findByName(ownerName);

        List<String> ids = orderIds.stream().map(UUID::toString).toList();
        List<SelectOrderProperties> orderProperties = orderRepository.findAllById(ids)
                .stream()
                .map(order -> new SelectOrderProperties(SelectType.FOR_MATCH, order))
                .toList();
        owner.getSelected().addAll(orderProperties);
        Owner saved = ownerService.save(owner);
        return saved.getSelected().stream()
                .filter(properties -> properties.getType().equals(SelectType.FOR_MATCH))
                .map(properties -> orderMapper.toDto(properties.getOrder()))
                .toList();
    }

    public OrderDto update(String orderId, OrderDto orderDto, String ownerName) {
        if (!orderDto.getStatus().equals(OrderStatus.DRAFT)) {
            throw new UnsupportedOperationException();
        }
        Order order = orderRepository.findById(orderId).orElseThrow(RuntimeException::new)
                .withCategory(orderDto.getCategory())
                .withCountry(orderDto.getCountry())
                .withTitle(orderDto.getTitle())
                .withLastModifiedDate(LocalDateTime.now())
                .withLastModifiedBy(ownerName);
        Order updated = orderRepository.save(order);
        return orderMapper.toDto(updated);
    }

    public OrderDto changeStatus(String ownerName, String orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(RuntimeException::new)
                .withStatus(orderStatus)
                .withLastModifiedDate(LocalDateTime.now())
                .withLastModifiedBy(ownerName);
        Order changed = orderRepository.save(order);
        return orderMapper.toDto(changed);
    }

    public void delete(String orderId) {
        orderRepository.findById(orderId).ifPresent(orderRepository::delete);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OrderDto> selectForTransaction(String ownerName, UUID orderId) {
        Owner owner = ownerService.findByName(ownerName);
        owner.getSelected().forEach(
                selected -> {
                    if (orderId.toString().equals(selected.getOrder().getId())) {
                        selected.setType(SelectType.FOR_TRANSACTION);
                    }
                }
        );
        Owner saved = ownerService.save(owner);
        return saved.getSelected().stream()
                .filter(properties -> properties.getType().equals(SelectType.FOR_MATCH))
                .map(properties -> orderMapper.toDto(properties.getOrder()))
                .toList();
    }
}
