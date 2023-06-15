package ge.elzhart.service.transaction;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.api.dto.transaction.TransactionGraphDto;
import ge.elzhart.api.mapper.RecordSearchMapper;
import ge.elzhart.model.domain.order.Order;
import ge.elzhart.model.domain.order.OrderStatus;
import ge.elzhart.model.domain.user.User;
import ge.elzhart.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionGraphService {

    private final Neo4jClient neo4jClient;
    private final DatabaseSelectionProvider databaseSelectionProvider;
    private final UserService userService;

    public List<TransactionGraphDto> findTransactionGraph(String username, String orderId) {

        Collection<Map<String, Object>> results = neo4jClient
                .query("MATCH p=(:User {username: $name})-[:SELECTED {type: 'FOR_TRANSACTION'}]->(order)-[r:SELECTED|CREATED*]-(:User {username: $name})\n"
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
        for (InternalNode next : nodes) {
            if (next.hasLabel("User")) {
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

    public void transactionOrders(List<TransactionGraphDto> transactionGraph) {
        Map<String, TransactionGraphDto> usernames = transactionGraph.stream()
                .collect(Collectors.toMap(TransactionGraphDto::getUsername, Function.identity()));
        Map<String, TransactionGraphDto> orderIds = transactionGraph.stream()
                .collect(Collectors.toMap(TransactionGraphDto::getOrderId, Function.identity()));

        Map<String, Order> transactionPair = new HashMap<>();
        Map<String, String> detachPair = new HashMap<>();

        List<User> users = userService.findAllByNameIn(usernames.keySet());

        users.forEach(user -> {
            Iterator<Order> iterator = user.getCreated().iterator();
            while (iterator.hasNext()) {
                Order next = iterator.next();
                TransactionGraphDto transaction = orderIds.get(next.getId());
                if (transaction != null) {
                    detachPair.put(user.getUsername(), next.getId());
                    transactionPair.put(transaction.getUsername(), next);
                }
            }
        });

        users.forEach(user -> {
            if (transactionPair.containsKey(user.getUsername())) {
                Order order = transactionPair.get(user.getUsername());
                order.setStatus(OrderStatus.DRAFT);
                user.getCreated().add(order);
            }
        });

        userService.saveAll(users);
        detachPair.forEach(userService::detachOrderFromUser);
    }

    private String database() {
        return databaseSelectionProvider.getDatabaseSelection().getValue();
    }
}
