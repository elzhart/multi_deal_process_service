package ge.elzhart.api;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import ge.elzhart.messaging.OrderEventPublisher;
import ge.elzhart.model.OrderStatus;
import ge.elzhart.model.SelectType;
import ge.elzhart.service.OrderSearchService;
import ge.elzhart.service.OrderService;
import ge.elzhart.api.dto.OrderDto;
import ge.elzhart.api.dto.OrderFilterDto;
import ge.elzhart.api.dto.SortDto;
import lombok.RequiredArgsConstructor;

/**
 * @author Artur Elzhanov
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public
class OrderController {
    private final OrderService orderService;
    private final OrderSearchService orderSearchService;
    private final OrderEventPublisher orderEventPublisher;


    @GetMapping("/created")
    public List<OrderDto> findSelfCreated(Principal principal) {
        String ownerName = principal.getName();
        return orderService.findCreatedByOwnerName(ownerName);
    }

    @GetMapping("/posted")
    public List<OrderDto> findSelfPosted(Principal principal) {
        String ownerName = principal.getName();
        return orderService.findPostedByOwnerName(ownerName);
    }

    @GetMapping("/selected/for_match")
    public List<OrderDto> findSelectedForMatch(Principal principal) {
        String ownerName = principal.getName();
        return orderService.findSelected(ownerName, SelectType.FOR_MATCH);
    }

    @GetMapping("/selected/for_transaction")
    public List<OrderDto> findSelectedForTransaction(Principal principal) {
        String ownerName = principal.getName();
        return orderService.findSelected(ownerName, SelectType.FOR_TRANSACTION);
    }

    @PostMapping("/search")
    public Collection<OrderDto> searchPosted(
            @RequestParam(value = "sort", required = false) String[] sortBy,
            OrderFilterDto orderFilterDto,
            Pageable pageable,
            Principal principal) {
        SortDto sortDto = new SortDto(sortBy, "title");
        String ownerName = principal.getName();
        return orderSearchService.orderSearchByFilter(orderFilterDto, pageable, sortDto, ownerName);
    }

    @PostMapping("/create")
    public List<OrderDto> create(OrderDto orderDto, Principal principal) {
        String ownerName = principal.getName();
        return orderService.create(ownerName, orderDto);
    }

    @PutMapping("/update/{orderId}")
    public OrderDto update(@PathVariable String orderId, OrderDto orderDto, Principal principal) {
        String ownerName = principal.getName();
        return orderService.update(orderId, orderDto, ownerName);
    }

    @PutMapping("/post/{orderId}")
    public OrderDto post(@PathVariable String orderId, Principal principal) {
        String ownerName = principal.getName();
        return orderService.changeStatus(orderId, ownerName, OrderStatus.POSTED);
    }

    @PutMapping("/close/{orderId}")
    public OrderDto close(@PathVariable String orderId, Principal principal) {
        String ownerName = principal.getName();
        return orderService.changeStatus(orderId, ownerName, OrderStatus.CLOSED);
    }

    @DeleteMapping("/delete/{orderId}")
    public void delete(@PathVariable String orderId) {
        orderService.delete(orderId);
    }

    @PostMapping("/select/for_match")
    public List<OrderDto> select(List<UUID> orderIds, Principal principal) {
        String ownerName = principal.getName();
        return orderService.selectForMatch(ownerName, orderIds);
    }

    @GetMapping("/matched")
    public List<List<OrderDto>> findMatched(Principal principal) {
        String ownerName = principal.getName();
        return orderService.findMatchedByOwnerName(ownerName);
    }

    @PostMapping("/select/for_transaction")
    public List<OrderDto> selectForTransaction(UUID orderId, Principal principal) {
        String ownerName = principal.getName();
        List<OrderDto> selected = orderService.selectForTransaction(ownerName, orderId);
        orderEventPublisher.publish(ownerName, orderId.toString());
        return selected;
    }
}
