package ge.elzhart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ge.elzhart.api.OrderController;
import ge.elzhart.api.dto.OrderDto;
import ge.elzhart.api.dto.OrderFilterDto;
import ge.elzhart.api.dto.SortDto;
import ge.elzhart.model.OrderCategory;
import ge.elzhart.model.OrderStatus;
import ge.elzhart.model.SelectType;
import ge.elzhart.service.OrderSearchService;
import ge.elzhart.service.OrderService;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderIntegrationTest extends AbstractIntegrationTest {
    private static final String ALINA_USER = "Alina";
    private static final String PITER_USER = "Piter";
    private static final String MIKE_USER = "Mike";
    private static final String ROPE_ORDER_TITLE = "Rope-75m";
    private static final String SHOES_ORDER_TITLE = "Climbing_shoes";
    private static final String BARBECUE_ORDER_TITLE = "Barbecue";
    private static final String ROD_ORDER_TITLE = "Fishing rod";

    private static final String ORDER_COUNTRY = "TH";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderSearchService orderSearchService;

    @Autowired
    private OrderController orderController;

    @BeforeEach
    void beforeTest(@Autowired Driver driver) {
        initDb(driver);
    }

    @Test
    void shouldPostAndGetPostedOrders() {
        assertThat(orderService.findPostedByOwnerName(ALINA_USER)).hasSize(1)
                .extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROPE_ORDER_TITLE);
        OrderDto newOrder = new OrderDto()
                .setTitle(SHOES_ORDER_TITLE)
                .setCategory(OrderCategory.CLIMBING)
                .setStatus(OrderStatus.DRAFT)
                .setCountry("RU");
        List<OrderDto> createdOrders = orderService.create(ALINA_USER, newOrder);
        assertThat(orderService.findCreatedByOwnerName(ALINA_USER)).hasSize(1);
        assertThat(orderService.findPostedByOwnerName(ALINA_USER)).hasSize(1);

        List<OrderDto> draftOrders = createdOrders.stream()
                .filter(orderDto -> orderDto.getStatus().equals(OrderStatus.DRAFT))
                .collect(Collectors.toList());
        assertThat(draftOrders).hasSize(1);
        OrderDto draft = draftOrders.get(0);
        draft.setCountry("TH");
        orderService.update(draft.getId(), draft, ALINA_USER);
        orderService.changeStatus(ALINA_USER, draft.getId(), OrderStatus.POSTED);
        assertThat(orderService.findPostedByOwnerName(ALINA_USER)).hasSize(2)
                .extracting(OrderDto::getCountry)
                .containsExactlyInAnyOrder("TH", "TH");
        orderService.delete(draft.getId());
        assertThat(orderService.findPostedByOwnerName(ALINA_USER)).hasSize(1);
    }

    @Test
    void shouldSelectAndGetMatchedOrders() {
        List<OrderDto> selfSelected = orderService.findSelected(ALINA_USER, SelectType.FOR_MATCH);
        assertThat(selfSelected).hasSize(0);
        orderService.selectForMatch(ALINA_USER, List.of(UUID.fromString("9574ef44-eef7-11ed-a05b-0242ac120003")));
        orderService.selectForMatch(PITER_USER, List.of(UUID.fromString("9574ea30-eef7-11ed-a05b-0242ac120003")));
        orderService.selectForMatch(MIKE_USER, List.of(UUID.fromString("9574ed46-eef7-11ed-a05b-0242ac120003"), UUID.fromString("9574ea30-eef7-11ed-a05b-0242ac120003")));
        selfSelected = orderService.findSelected(ALINA_USER, SelectType.FOR_MATCH);
        assertThat(selfSelected).hasSize(1);
        List<List<OrderDto>> matchedList = orderService.findMatchedByOwnerName(ALINA_USER);
        assertThat(matchedList).hasSize(2);
        List<OrderDto> shortGraph = matchedList.get(0);
        assertThat(shortGraph).hasSize(2).extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROPE_ORDER_TITLE, BARBECUE_ORDER_TITLE);
        List<OrderDto> longGraph = matchedList.get(1);
        assertThat(longGraph).hasSize(3).extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROPE_ORDER_TITLE, BARBECUE_ORDER_TITLE, ROD_ORDER_TITLE);
    }

    @Test
    void shouldSearchRightOrders() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        SortDto sortDto = new SortDto(new String[] {}, "order.title");

        OrderFilterDto orderTitleFilter = new OrderFilterDto().setTitle(ROPE_ORDER_TITLE);
        assertThat(orderSearchService.orderSearchByFilter(orderTitleFilter, pageRequest, sortDto, MIKE_USER)).hasSize(1);

        OrderFilterDto orderStatusFilter = new OrderFilterDto().setStatus(OrderStatus.POSTED);
        assertThat(orderSearchService.orderSearchByFilter(orderStatusFilter, pageRequest, sortDto, MIKE_USER)).hasSize(2);

        OrderFilterDto orderCategoryFilter = new OrderFilterDto().setCategory(OrderCategory.FISHING);
        assertThat(orderSearchService.orderSearchByFilter(orderCategoryFilter, pageRequest, sortDto, MIKE_USER)).hasSize(1);

        OrderFilterDto orderCountryFilter = new OrderFilterDto().setCountry(ORDER_COUNTRY);
        assertThat(orderSearchService.orderSearchByFilter(orderCountryFilter, pageRequest, sortDto, MIKE_USER)).hasSize(1);

        assertThat(orderSearchService.orderSearchByFilter(
                new OrderFilterDto(),
                PageRequest.of(1, 1),
                new SortDto(new String[] {"order.title", "desc"}, ""),
                MIKE_USER))
                .hasSize(1)
                .extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROD_ORDER_TITLE);
    }

    @Test
    void shouldRealizeTransaction() throws InterruptedException {
        orderController.select(
                List.of(UUID.fromString("9574ef44-eef7-11ed-a05b-0242ac120003")),
                new TestPrincipal().setTestUsername(ALINA_USER)
        );
        orderController.select(
                List.of(UUID.fromString("9574ea30-eef7-11ed-a05b-0242ac120003")),
                new TestPrincipal().setTestUsername(PITER_USER)
        );
        orderController.select(
                List.of(UUID.fromString("9574ed46-eef7-11ed-a05b-0242ac120003")),
                new TestPrincipal().setTestUsername(MIKE_USER)
        );

        orderController.selectForTransaction(
                UUID.fromString("9574ef44-eef7-11ed-a05b-0242ac120003"),
                new TestPrincipal().setTestUsername(ALINA_USER)
        );
        orderController.selectForTransaction(
                UUID.fromString("9574ea30-eef7-11ed-a05b-0242ac120003"),
                new TestPrincipal().setTestUsername(PITER_USER)
        );
        orderController.selectForTransaction(
                UUID.fromString("9574ed46-eef7-11ed-a05b-0242ac120003"),
                new TestPrincipal().setTestUsername(MIKE_USER)
        );

        Thread.sleep(10000);
        assertThat(orderController.findSelfCreated(new TestPrincipal().setTestUsername(ALINA_USER)))
                .hasSize(1)
                .extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(BARBECUE_ORDER_TITLE);
        assertThat(orderController.findSelfCreated(new TestPrincipal().setTestUsername(PITER_USER)))
                .hasSize(1)
                .extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROPE_ORDER_TITLE);
        assertThat(orderController.findSelfCreated(new TestPrincipal().setTestUsername(MIKE_USER)))
                .hasSize(1)
                .extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROD_ORDER_TITLE);
    }

}
