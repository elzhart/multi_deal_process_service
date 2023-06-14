package ge.elzhart.model.repository;

import org.neo4j.driver.internal.value.ListValue;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import ge.elzhart.model.domain.Order;
import ge.elzhart.model.domain.SelectType;

public interface OrderRepository extends Neo4jRepository<Order, String> {

    @Query("MATCH (:User {username: $name})-[:CREATED]->(order)\n"
            + "WHERE order.status = 'DRAFT'\n"
            + "RETURN order")
    List<Order> findCreatedByUsername(@Param("name") String username);

    @Query("MATCH (:User {username: $name})-[:CREATED]->(order)\n"
            + "WHERE order.status = 'POSTED'\n"
            + "RETURN order")
    List<Order> findPostedByUsername(@Param("name") String username);

    @Query("MATCH (:User {username: $name})-[selected: SELECTED]->(order)\n"
            + "WHERE order.status = 'POSTED'\n"
            + "AND selected.type = $type\n"
            + "RETURN order")
    List<Order> findSelectedByUsername(@Param("name") String username, @Param("type") SelectType selectType);

    @Query("MATCH p=(:User {username: $name})-[selected: SELECTED]->(order)-[*]-(:User {username: $name})\n"
            + "WHERE order.status = 'POSTED'\n"
            + "AND selected.type = $type\n"
            + "RETURN nodes(p)")
    List<ListValue> findMatchedByUsername(@Param("name") String username, @Param("type") SelectType selectType);
}
