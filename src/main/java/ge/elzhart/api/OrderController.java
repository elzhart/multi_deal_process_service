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
import ge.elzhart.model.domain.order.OrderStatus;
import ge.elzhart.model.domain.user.Role;
import ge.elzhart.model.domain.user.SelectType;
import ge.elzhart.service.order.OrderSearchService;
import ge.elzhart.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

/**
 * @author Artur Elzhanov
 */
@RestController
@RequiredArgsConstructor
@RolesAllowed(Role.OWNER)
@RequestMapping("/api/order")
@Tag(name = "Orders", description = "Use for actions related to orders")
public
class OrderController {
    private final OrderService orderService;
    private final OrderSearchService orderSearchService;
    private final OrderEventPublisher orderEventPublisher;

    @PostMapping("/create")
    @Operation(summary = "create a new order by user")
    public List<OrderDto> create(@RequestBody OrderDto orderDto, Principal principal) {
        String username = principal.getName();
        return orderService.create(username, orderDto);
    }

    @GetMapping("/created")
    @Operation(summary = "find self created orders for user")
    public List<OrderDto> findSelfCreated(Principal principal) {
        String username = principal.getName();
        return orderService.findCreatedByUsername(username);
    }

    @PutMapping("/update/{orderId}")
    @Operation(summary = "update an order with a given identifier")
    public OrderDto update(
            @Parameter(description = "id of order to be updated") @PathVariable String orderId,
            @RequestBody OrderDto orderDto,
            Principal principal
    ) {
        String username = principal.getName();
        return orderService.update(orderId, orderDto, username);
    }

    @DeleteMapping("/delete/{orderId}")
    @Operation(summary = "delete an order with a given identifier")
    public void delete(
            @Parameter(description = "id of order to be deleted")  @PathVariable String orderId
    ) {
        orderService.delete(orderId);
    }

    @PutMapping("/post/{orderId}")
    @Operation(summary = "post an order with a given identifier")
    public OrderDto post(
            @Parameter(description = "id of order to be posted") @PathVariable String orderId,
            Principal principal
    ) {
        String username = principal.getName();
        return orderService.changeStatus(username, orderId, OrderStatus.POSTED);
    }

    @GetMapping("/posted")
    @Operation(summary = "find self posted orders for user")
    public List<OrderDto> findSelfPosted(Principal principal) {
        String username = principal.getName();
        return orderService.findPostedByUsername(username);
    }

    @PostMapping("/search")
    @Operation(summary = "get orders with optional filters")
    @Parameters({
            @Parameter(name = "sort", hidden = true, example = "order.title"),
            @Parameter(name = "page", description = "Zero-based page index (0..N)", example = "0"),
            @Parameter(name = "size", description = "The size of the page to be returned", example = "10")
    })
    public RestPage<OrderDto> searchPosted(
            @RequestBody OrderFilterDto orderFilterDto,
            Pageable pageable,
            Principal principal) {
        String userName = principal.getName();
        orderFilterDto.setUsername(userName);
        return new RestPage<>(orderSearchService.searchByFilter(orderFilterDto, pageable));
    }

    @PostMapping("/select/for_match")
    @Operation(summary = "select an orders for match with a given identifiers")
    public List<OrderDto> select(@RequestBody SelectOrdersRequest selectOrdersRequest, Principal principal) {
        String username = principal.getName();
        List<UUID> orderIds = selectOrdersRequest.getOrderIds();
        return orderService.selectForMatch(username, orderIds);
    }

    @GetMapping("/selected/for_match")
    @Operation(summary = "find orders for match selected by user")
    public List<OrderDto> findSelectedForMatch(Principal principal) {
        String username = principal.getName();
        return orderService.findSelected(username, SelectType.FOR_MATCH);
    }

    @GetMapping("/matched")
    @Operation(summary = "find matched orders chains for user")
    public List<List<OrderDto>> findMatched(Principal principal) {
        String username = principal.getName();
        return orderService.findMatchedByUsername(username);
    }

    @PutMapping("/select/for_transaction/{orderId}")
    @Operation(summary = "select an order for transaction with a given identifier")
    public List<OrderDto> selectForTransaction(
            @Parameter(description = "id of order to be selected") @PathVariable UUID orderId,
            Principal principal
    ) {
        String username = principal.getName();
        List<OrderDto> selected = orderService.selectForTransaction(username, orderId);
        orderEventPublisher.publish(username, orderId.toString());
        return selected;
    }

    @GetMapping("/selected/for_transaction")
    @Operation(summary = "find orders for transaction selected by user")
    public List<OrderDto> findSelectedForTransaction(Principal principal) {
        String username = principal.getName();
        return orderService.findSelected(username, SelectType.FOR_TRANSACTION);
    }

    @PutMapping("/close/{orderId}")
    @Operation(summary = "delete an order with a given identifier")
    public OrderDto close(
            @Parameter(description = "id of order to be closed") @PathVariable String orderId,
            Principal principal
    ) {
        String username = principal.getName();
        return orderService.changeStatus(username, orderId, OrderStatus.CLOSED);
    }
}
