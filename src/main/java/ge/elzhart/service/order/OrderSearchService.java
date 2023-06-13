package ge.elzhart.service.order;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.api.dto.order.OrderFilterDto;
import ge.elzhart.api.mapper.RecordSearchMapper;
import ge.elzhart.service.SearchService;

@Service
public class OrderSearchService extends SearchService<OrderFilterDto, OrderDto> {

    public OrderSearchService(Neo4jClient neo4jClient, DatabaseSelectionProvider databaseSelectionProvider) {
        super(neo4jClient, databaseSelectionProvider);
    }

    @NotNull
    @Override
    protected String generateQuery(OrderFilterDto orderFilterDto) {
        StringBuilder stringBuilder = new StringBuilder(
                "MATCH (owner: Owner)-[:CREATED]->(order)\n"
                        + "WHERE owner.name <> $ownerName\n");
        if (StringUtils.isNotBlank(orderFilterDto.getTitle())) {
            stringBuilder.append("AND order.title = $title\n");
        }
        if (orderFilterDto.getCategory() != null) {
            stringBuilder.append("AND order.category = $category\n");
        }
        if (StringUtils.isNotBlank(orderFilterDto.getCountry())) {
            stringBuilder.append("AND order.country = $country\n");
        }
        if (orderFilterDto.getStatus() != null) {
            stringBuilder.append("AND order.status = $status\n");
        }

        stringBuilder.append(
                "RETURN order\n"
                        + "ORDER BY $order\n"
                        + "SKIP $offset\n"
                        + "LIMIT $pageSize");

        return stringBuilder.toString();
    }

    @NotNull
    @Override
    protected Map<String, Object> generateParams(OrderFilterDto orderFilterDto, Pageable pageable) {
        Map<String, Object> params = new java.util.HashMap<>(Map.of(
                "ownerName", orderFilterDto.getUserName(),
                "order", pageable.getSort().stream().findFirst().toString(),
                "offset", pageable.getOffset(),
                "pageSize", pageable.getPageSize()
        ));

        if (StringUtils.isNotBlank(orderFilterDto.getTitle())) {
            params.put("title", orderFilterDto.getTitle());
        }
        if (orderFilterDto.getCategory() != null) {
            params.put("category", orderFilterDto.getCategory().name());
        }
        if (StringUtils.isNotBlank(orderFilterDto.getCountry())) {
            params.put("country", orderFilterDto.getCountry());
        }
        if (orderFilterDto.getStatus() != null) {
            params.put("status", orderFilterDto.getStatus().name());
        }
        return params;
    }

    @Override
    protected Class<OrderDto> getTargetClass() {
        return OrderDto.class;
    }

    @Override
    protected OrderDto toResult(TypeSystem typeSystem, Record record) {
        Value order = record.get("order");
        return RecordSearchMapper.fromRecordToOrder(order);
    }

    @Override
    protected String totalCountQuery(OrderFilterDto orderFilterDto) {
        StringBuilder stringBuilder = new StringBuilder(
                "MATCH (owner: Owner)-[:CREATED]->(order)\n"
                        + "WHERE owner.name <> $ownerName\n");
        if (StringUtils.isNotBlank(orderFilterDto.getTitle())) {
            stringBuilder.append("AND order.title = $title\n");
        }
        if (orderFilterDto.getCategory() != null) {
            stringBuilder.append("AND order.category = $category\n");
        }
        if (StringUtils.isNotBlank(orderFilterDto.getCountry())) {
            stringBuilder.append("AND order.country = $country\n");
        }
        if (orderFilterDto.getStatus() != null) {
            stringBuilder.append("AND order.status = $status\n");
        }

        stringBuilder.append("RETURN count(distinct order)");

        return stringBuilder.toString();
    }

    @Override
    protected Map<String, Object> generateTotalCountParams(OrderFilterDto orderFilterDto) {
        Map<String, Object> params = new HashMap<>(Map.of("ownerName", orderFilterDto.getUserName()));

        if (StringUtils.isNotBlank(orderFilterDto.getTitle())) {
            params.put("title", orderFilterDto.getTitle());
        }
        if (orderFilterDto.getCategory() != null) {
            params.put("category", orderFilterDto.getCategory().name());
        }
        if (StringUtils.isNotBlank(orderFilterDto.getCountry())) {
            params.put("country", orderFilterDto.getCountry());
        }
        if (orderFilterDto.getStatus() != null) {
            params.put("status", orderFilterDto.getStatus().name());
        }
        return params;
    }
}
