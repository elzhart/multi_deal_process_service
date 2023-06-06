package ge.elzhart.service;


import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ge.elzhart.api.dto.OrderDto;
import ge.elzhart.api.dto.OrderFilterDto;
import ge.elzhart.api.dto.SortDto;
import ge.elzhart.api.dto.TransactionGraphDto;
import ge.elzhart.api.mapper.RecordSearchMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSearchService {

    private final Neo4jClient neo4jClient;
    private final DatabaseSelectionProvider databaseSelectionProvider;

    public Collection<OrderDto> orderSearchByFilter(OrderFilterDto orderFilterDto, Pageable pageable, SortDto sortDto, String ownerName) {
        return this.neo4jClient
                .query(generateQuery(orderFilterDto))
                .in(database())
                .bindAll(generateParams(orderFilterDto, pageable, sortDto, ownerName))
                .fetchAs(OrderDto.class)
                .mappedBy(this::toOrderDto)
                .all();
    }

    @NotNull
    private static Map<String, Object> generateParams(OrderFilterDto orderFilterDto, Pageable pageable, SortDto sortDto, String ownerName) {
        Map<String, Object> params = new java.util.HashMap<>(Map.of(
                "ownerName", ownerName,
                "order", sortDto.toParam(),
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

    private String generateQuery(OrderFilterDto orderFilterDto) {
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

    private OrderDto toOrderDto(TypeSystem ignored, org.neo4j.driver.Record record) {
        Value order = record.get("order");
        return RecordSearchMapper.fromRecordToOrder(order);
    }

    private String database() {
        return databaseSelectionProvider.getDatabaseSelection().getValue();
    }

    public List<TransactionGraphDto> findTransactionGraph(String username, String orderId) {

        Collection<Map<String, Object>> results = neo4jClient
                .query("MATCH p=(:Owner {name: $name})-[:SELECTED {type: 'FOR_TRANSACTION'}]->(order)-[r:SELECTED|CREATED*]-(:Owner {name: $name})\n"
                        + "WHERE order.id = $id\n"
                        + "AND ALL(rel in r where ((TYPE(rel)= 'SELECTED' and rel.type = 'FOR_TRANSACTION') or TYPE(rel)= 'CREATED'))\n"
                        + "RETURN nodes(p), relationships(p)")
                .in(database())
                .bindAll(Map.of("name", username, "id", orderId))
                .fetch().all();

        return results.stream().findFirst().map(result -> {
            Map<String, String> relationshipMap = new HashMap<>();
            List<InternalRelationship> relationships = (List<InternalRelationship>) result.get("relationships(p)");
            fillRelationshipMap(relationships, relationshipMap);

            List<InternalNode> nodes = (List<InternalNode>) result.get("nodes(p)");
            return getTransactionGraphs(nodes, relationshipMap);
        }).orElse(new ArrayList<>());
    }

    private static void fillRelationshipMap(List<InternalRelationship> relationships, Map<String, String> relationshipMap) {
        Iterator<InternalRelationship> iterator = relationships.iterator();
        while (iterator.hasNext()) {
            InternalRelationship next = iterator.next();
            if (!next.type().equals("SELECTED")) {
                continue;
            }
            relationshipMap.put(next.startNodeElementId(), next.endNodeElementId());
        }
    }

    @NotNull
    private static List<TransactionGraphDto> getTransactionGraphs(List<InternalNode> nodes, Map<String, String> map) {
        Map<String, TransactionGraphDto> transactionGraphs = new HashMap<>();
        Map<String, OrderDto> orders = new HashMap<>();
        Iterator<InternalNode> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            InternalNode next = iterator.next();

            if (next.hasLabel("Owner")) {
                TransactionGraphDto transactionGraphDto = RecordSearchMapper.fromRecordToTransaction(next.asValue());
                transactionGraphs.put(next.elementId(), transactionGraphDto);
            }

            if (next.hasLabel("Order")) {
                OrderDto orderDto = RecordSearchMapper.fromRecordToOrder(next.asValue());
                orders.put(next.elementId(), orderDto);
                TransactionGraphDto transactionGraphDto = transactionGraphs.get(orderDto.getId());
                if (transactionGraphDto != null) {
                    transactionGraphDto.setOrderTitle(orderDto.getTitle());
                }
            }
        }

        transactionGraphs.forEach((s, transactionGraphDto) -> {
            OrderDto orderDto = orders.get(map.get(s));
            if (orderDto != null) {
                transactionGraphDto.setOrderId(orderDto.getId());
                transactionGraphDto.setOrderTitle(orderDto.getTitle());
            }

        });
        return transactionGraphs.values().stream().toList();
    }
}
