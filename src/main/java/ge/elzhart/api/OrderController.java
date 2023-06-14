package ge.elzhart.api;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.api.dto.order.OrderFilterDto;
import ge.elzhart.api.dto.RestPage;
import ge.elzhart.api.dto.order.SelectOrdersRequest;
import ge.elzhart.messaging.OrderEventPublisher;
import ge.elzhart.model.domain.OrderStatus;
import ge.elzhart.model.domain.Role;
import ge.elzhart.model.domain.SelectType;
import ge.elzhart.service.order.OrderSearchService;
import ge.elzhart.service.order.OrderService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

/**
 * @author Artur Elzhanov
 */
@RestController
@RequiredArgsConstructor
@RolesAllowed(Role.OWNER)
@RequestMapping("/api/order")
public
class OrderController {
    private final OrderService orderService;
    private final OrderSearchService orderSearchService;
    private final OrderEventPublisher orderEventPublisher;

    @PostMapping("/create")
    public List<OrderDto> create(@RequestBody OrderDto orderDto, Principal principal) {
        String username = principal.getName();
        return orderService.create(username, orderDto);
    }

    @GetMapping("/created")
    public List<OrderDto> findSelfCreated(Principal principal) {
        String username = principal.getName();
        return orderService.findCreatedByUsername(username);
    }

    @PutMapping("/update/{orderId}")
    public OrderDto update(@PathVariable String orderId, @RequestBody OrderDto orderDto, Principal principal) {
        String username = principal.getName();
        return orderService.update(orderId, orderDto, username);
    }

    @DeleteMapping("/delete/{orderId}")
    public void delete(@PathVariable String orderId) {
        orderService.delete(orderId);
    }

    @PutMapping("/post/{orderId}")
    public OrderDto post(@PathVariable String orderId, Principal principal) {
        String username = principal.getName();
        return orderService.changeStatus(username, orderId, OrderStatus.POSTED);
    }

    @GetMapping("/posted")
    public List<OrderDto> findSelfPosted(Principal principal) {
        String username = principal.getName();
        return orderService.findPostedByUsername(username);
    }

    @PostMapping("/search")
    public RestPage<OrderDto> searchPosted(
            @RequestBody OrderFilterDto orderFilterDto,
            Pageable pageable,
            Principal principal) {
        String userName = principal.getName();
        orderFilterDto.setUsername(userName);
        return new RestPage<>(orderSearchService.searchByFilter(orderFilterDto, pageable));
    }

    @PostMapping("/select/for_match")
    public List<OrderDto> select(@RequestBody SelectOrdersRequest selectOrdersRequest, Principal principal) {
        String username = principal.getName();
        List<UUID> orderIds = selectOrdersRequest.getOrderIds();
        return orderService.selectForMatch(username, orderIds);
    }

    @GetMapping("/selected/for_match")
    public List<OrderDto> findSelectedForMatch(Principal principal) {
        String username = principal.getName();
        return orderService.findSelected(username, SelectType.FOR_MATCH);
    }

    @GetMapping("/matched")
    public List<List<OrderDto>> findMatched(Principal principal) {
        String username = principal.getName();
        return orderService.findMatchedByUsername(username);
    }

    @PutMapping("/select/for_transaction/{orderId}")
    public List<OrderDto> selectForTransaction(@PathVariable UUID orderId, Principal principal) {
        String username = principal.getName();
        List<OrderDto> selected = orderService.selectForTransaction(username, orderId);
        orderEventPublisher.publish(username, orderId.toString());
        return selected;
    }

    @GetMapping("/selected/for_transaction")
    public List<OrderDto> findSelectedForTransaction(Principal principal) {
        String username = principal.getName();
        return orderService.findSelected(username, SelectType.FOR_TRANSACTION);
    }

    @PutMapping("/close/{orderId}")
    public OrderDto close(@PathVariable String orderId, Principal principal) {
        String username = principal.getName();
        return orderService.changeStatus(username, orderId, OrderStatus.CLOSED);
    }
}
