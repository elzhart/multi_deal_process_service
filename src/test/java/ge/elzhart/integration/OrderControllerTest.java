package ge.elzhart.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import ge.elzhart.api.dto.RestPage;
import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.api.dto.order.OrderFilterDto;
import ge.elzhart.api.dto.order.SelectOrdersRequest;
import ge.elzhart.integration.data.OrderTestDataFactory;
import ge.elzhart.integration.data.UserTestDataFactory;
import ge.elzhart.model.domain.OrderCategory;
import ge.elzhart.model.domain.OrderStatus;
import ge.elzhart.model.domain.Role;
import ge.elzhart.model.domain.SelectType;

import static ge.elzhart.util.JsonHelper.fromJson;
import static ge.elzhart.util.JsonHelper.toJson;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = Role.OWNER)
public class OrderControllerTest extends AbstractIntegrationTest {
    private static final String ALINA_USER = "Alina";
    private static final String PITER_USER = "Piter";
    private static final String MIKE_USER = "Mike";
    private static final String ROPE_ORDER_TITLE = "Rope-75m";
    private static final String SHOES_ORDER_TITLE = "Climbing_shoes";
    private static final String BARBECUE_ORDER_TITLE = "Barbecue";
    private static final String ROD_ORDER_TITLE = "Fishing rod";

    private static final String ORDER_COUNTRY = "TH";

    private static final Set<String> deleteAfterTest = new CopyOnWriteArraySet<>();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderTestDataFactory orderTestDataFactory;
    @Autowired
    private UserTestDataFactory userTestDataFactory;
//    @Autowired
//    private WebTestClient webTestClient;

    @BeforeEach
    void beforeTest() {
        userTestDataFactory.createUser(ALINA_USER, ALINA_USER);
        userTestDataFactory.createUser(MIKE_USER, MIKE_USER);
        userTestDataFactory.createUser(PITER_USER, PITER_USER);
    }

    @Test
    @WithMockUser(roles = Role.OWNER, username = ALINA_USER)
    void testCRUD() throws Exception {
        OrderDto newOrder = new OrderDto()
                .setTitle(SHOES_ORDER_TITLE)
                .setCategory(OrderCategory.CLIMBING)
                .setStatus(OrderStatus.DRAFT)
                .setCountry("RU");

        this.mockMvc
                .perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, newOrder)))
                .andExpect(status().isOk());

        MvcResult createResult = this.mockMvc
                .perform(get("/api/order/created"))
                .andExpect(status().isOk()).andReturn();

        List<OrderDto> createdOrders = fromJson(objectMapper, createResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        OrderDto draftOrder = createdOrders.stream()
                .filter(orderDto -> orderDto.getStatus().equals(OrderStatus.DRAFT))
                .findFirst().orElse(null);
        assertThat(draftOrder).isNotNull();
        assertThat(draftOrder.getTitle()).isEqualTo(SHOES_ORDER_TITLE);
        assertThat(draftOrder.getCategory()).isEqualTo(OrderCategory.CLIMBING);
        assertThat(draftOrder.getCountry()).isEqualTo("RU");
        draftOrder.setCountry("TH");

        MvcResult updateResult = this.mockMvc
                .perform(put(format("/api/order/update/%s", draftOrder.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, draftOrder)))
                .andExpect(status().isOk())
                .andReturn();


        OrderDto updateOrder = fromJson(objectMapper, updateResult.getResponse().getContentAsString(), OrderDto.class);
        assertThat(updateOrder.getCountry()).isEqualTo("TH");

        this.mockMvc
                .perform(delete(String.format("/api/order/delete/%s", updateOrder.getId())))
                .andExpect(status().isOk());


        createResult = this.mockMvc
                .perform(get("/api/order/created"))
                .andExpect(status().isOk()).andReturn();
        assertThat(createResult.getResponse().getContentAsString()).isEqualTo("[]");
    }

    @Test
    @WithMockUser(roles = Role.OWNER, username = ALINA_USER)
    void testPostOrder() throws Exception {
        OrderDto newOrder = orderTestDataFactory.createOrder(
                ALINA_USER, ROPE_ORDER_TITLE, OrderCategory.CLIMBING, OrderStatus.DRAFT, ORDER_COUNTRY
        );

        deleteAfterTest.add(newOrder.getId());

        this.mockMvc
                .perform(put(format("/api/order/post/%s", newOrder.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult postResult = this.mockMvc
                .perform(get("/api/order/posted"))
                .andExpect(status().isOk()).andReturn();

        List<OrderDto> createdOrders = fromJson(objectMapper, postResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        OrderDto postOrder = createdOrders.stream()
                .filter(orderDto -> orderDto.getStatus().equals(OrderStatus.POSTED))
                .findFirst().orElse(null);
        assertThat(postOrder).isNotNull();
        assertThat(postOrder.getTitle()).isEqualTo(ROPE_ORDER_TITLE);
        assertThat(postOrder.getCategory()).isEqualTo(OrderCategory.CLIMBING);
        assertThat(postOrder.getCountry()).isEqualTo(ORDER_COUNTRY);
    }

    @Test
    @WithMockUser(roles = Role.OWNER, username = MIKE_USER)
    void testSearchOrders() throws Exception {
        OrderDto postOrder1 = orderTestDataFactory.createOrder(
                ALINA_USER, ROPE_ORDER_TITLE, OrderCategory.CLIMBING, OrderStatus.POSTED, "TH"
        );

        OrderDto postOrder2 = orderTestDataFactory.createOrder(
                PITER_USER, ROD_ORDER_TITLE, OrderCategory.FISHING, OrderStatus.POSTED, "NO"
        );

        deleteAfterTest.addAll(List.of(postOrder1.getId(), postOrder2.getId()));

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "order.title"));

        OrderFilterDto orderTitleFilter = new OrderFilterDto().setTitle(ROPE_ORDER_TITLE);
        assertThat(execute("/api/order/search", orderTitleFilter, pageRequest)).hasSize(1);


        OrderFilterDto orderStatusFilter = new OrderFilterDto().setStatus(OrderStatus.POSTED);
        assertThat(execute("/api/order/search", orderStatusFilter, pageRequest)).hasSize(2);

        OrderFilterDto orderCategoryFilter = new OrderFilterDto().setCategory(OrderCategory.FISHING);
        assertThat(execute("/api/order/search", orderCategoryFilter, pageRequest)).hasSize(1);

        OrderFilterDto orderCountryFilter = new OrderFilterDto().setCountry(ORDER_COUNTRY);
        assertThat(execute("/api/order/search", orderCountryFilter, pageRequest)).hasSize(1);

        assertThat(execute("/api/order/search", orderStatusFilter, PageRequest.of(1, 1)))
                .hasSize(1)
                .extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROD_ORDER_TITLE);
    }

    @Test
    @WithMockUser(roles = Role.OWNER, username = ALINA_USER)
    void testSelectAndGetMatchedOrders() throws Exception {
        OrderDto postOrder1 = orderTestDataFactory.createOrder(
                ALINA_USER, ROPE_ORDER_TITLE, OrderCategory.CLIMBING, OrderStatus.POSTED, "TH"
        );

        OrderDto postOrder2 = orderTestDataFactory.createOrder(
                PITER_USER, ROD_ORDER_TITLE, OrderCategory.FISHING, OrderStatus.POSTED, "NO"
        );

        OrderDto postOrder3 = orderTestDataFactory.createOrder(
                MIKE_USER, BARBECUE_ORDER_TITLE, OrderCategory.COOKING, OrderStatus.POSTED, "US"
        );

        orderTestDataFactory.selectOrder(PITER_USER, postOrder1.getId(), SelectType.FOR_MATCH);
        orderTestDataFactory.selectOrder(MIKE_USER, postOrder2.getId(), SelectType.FOR_MATCH);

        deleteAfterTest.addAll(List.of(postOrder1.getId(), postOrder2.getId(), postOrder3.getId()));

        MvcResult selectedResult = this.mockMvc
                .perform(get("/api/order/selected/for_match"))
                .andExpect(status().isOk()).andReturn();
        assertThat(selectedResult.getResponse().getContentAsString()).isEqualTo("[]");

        SelectOrdersRequest selectOrdersRequest = new SelectOrdersRequest().setOrderIds(List.of(UUID.fromString(postOrder3.getId())));

        this.mockMvc
                .perform(post("/api/order/select/for_match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, selectOrdersRequest)))
                .andExpect(status().isOk());

        selectedResult = this.mockMvc
                .perform(get("/api/order/selected/for_match"))
                .andExpect(status().isOk()).andReturn();

        List<OrderDto> selectedOrders = fromJson(objectMapper, selectedResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        OrderDto selectedOrder = selectedOrders.stream()
                .filter(orderDto -> orderDto.getStatus().equals(OrderStatus.POSTED))
                .findFirst().orElse(null);
        assertThat(selectedOrder).isNotNull();
        assertThat(selectedOrder.getTitle()).isEqualTo(BARBECUE_ORDER_TITLE);
        assertThat(selectedOrder.getCategory()).isEqualTo(OrderCategory.COOKING);
        assertThat(selectedOrder.getCountry()).isEqualTo("US");

        MvcResult matchedResult = this.mockMvc
                .perform(get("/api/order/matched"))
                .andExpect(status().isOk()).andReturn();

        List<List<OrderDto>> matchedList = fromJson(objectMapper, matchedResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        ;
        assertThat(matchedList).hasSize(1);
        List<OrderDto> matched = matchedList.get(0);
        assertThat(matched).hasSize(3).extracting(OrderDto::getTitle)
                .containsExactlyInAnyOrder(ROPE_ORDER_TITLE, BARBECUE_ORDER_TITLE, ROD_ORDER_TITLE);
    }

    @Test
    @WithMockUser(roles = Role.OWNER, username = ALINA_USER)
    void shouldRealizeTransaction() throws Exception {
        OrderDto postOrder1 = orderTestDataFactory.createOrder(
                ALINA_USER, ROPE_ORDER_TITLE, OrderCategory.CLIMBING, OrderStatus.POSTED, "TH"
        );

        OrderDto postOrder2 = orderTestDataFactory.createOrder(
                PITER_USER, ROD_ORDER_TITLE, OrderCategory.FISHING, OrderStatus.POSTED, "NO"
        );

        OrderDto postOrder3 = orderTestDataFactory.createOrder(
                MIKE_USER, BARBECUE_ORDER_TITLE, OrderCategory.COOKING, OrderStatus.POSTED, "US"
        );

        orderTestDataFactory.selectOrder(PITER_USER, postOrder1.getId(), SelectType.FOR_MATCH);
        orderTestDataFactory.selectOrder(MIKE_USER, postOrder2.getId(), SelectType.FOR_MATCH);
        orderTestDataFactory.selectOrder(ALINA_USER, postOrder3.getId(), SelectType.FOR_MATCH);

        orderTestDataFactory.selectOrder(PITER_USER, postOrder1.getId(), SelectType.FOR_TRANSACTION);
        orderTestDataFactory.selectOrder(MIKE_USER, postOrder2.getId(), SelectType.FOR_TRANSACTION);

        deleteAfterTest.addAll(List.of(postOrder1.getId(), postOrder2.getId(), postOrder3.getId()));

        //TODO test sse event
//        new Thread(() -> {
//            FluxExchangeResult<TransactionEventDto> fluxExchangeResult = webTestClient
//                    .get()
//                    .uri("/api/sse/transactions")
//                    .accept(MediaType.TEXT_EVENT_STREAM)
//                    .exchange()
//                    .expectStatus().isOk()
//                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
//                    .returnResult(TransactionEventDto.class);
//
//            Flux<TransactionEventDto> eventFlux = fluxExchangeResult.getResponseBody();
//
//            StepVerifier.create(eventFlux.map(TransactionEventDto::getType))
//                    .expectSubscription()
//                    .expectNext("TRANSACTION_COMPLETE")
//                    .verifyComplete();
//        }).start();

        this.mockMvc
                .perform(put(format("/api/order/select/for_transaction/%s", postOrder3.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Thread.sleep(10000);

        MvcResult createResult = this.mockMvc
                .perform(get("/api/order/created"))
                .andExpect(status().isOk()).andReturn();

        List<OrderDto> createdOrders = fromJson(objectMapper, createResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        OrderDto transactionOrder = createdOrders.stream()
                .filter(orderDto -> orderDto.getStatus().equals(OrderStatus.DRAFT))
                .findFirst().orElse(null);
        assertThat(transactionOrder).isNotNull();
        assertThat(transactionOrder.getTitle()).isEqualTo(BARBECUE_ORDER_TITLE);
        assertThat(transactionOrder.getCategory()).isEqualTo(OrderCategory.COOKING);
        assertThat(transactionOrder.getCountry()).isEqualTo("US");
    }

    @AfterEach
    void tearDown() {
        orderTestDataFactory.deleteAll(deleteAfterTest);
        deleteAfterTest.clear();
        userTestDataFactory.deleteAll(Set.of(ALINA_USER, MIKE_USER, PITER_USER));
    }

    private RestPage<OrderDto> execute(String url, OrderFilterDto filter, PageRequest pageRequest) throws Exception {
        MvcResult result = this.mockMvc
                .perform(post(url)
                        .param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize()))
                        .param("sort", "order.title", "asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, filter)))
                .andExpect(status().isOk())
                .andReturn();

        return fromJson(objectMapper,
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
    }
}
