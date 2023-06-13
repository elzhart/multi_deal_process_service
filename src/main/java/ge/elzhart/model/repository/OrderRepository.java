package ge.elzhart.model.repository;

import org.neo4j.driver.internal.value.ListValue;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import ge.elzhart.model.domain.Order;
import ge.elzhart.model.domain.SelectType;

public interface OrderRepository extends Neo4jRepository<Order, String> {

    @Query("MATCH (:Owner {name: $name})-[:CREATED]->(order)\n"
            + "WHERE order.status = 'DRAFT'\n"
            + "RETURN order")
    List<Order> findCreatedByOwnerName(@Param("name") String name);

    @Query("MATCH (:Owner {name: $name})-[:CREATED]->(order)\n"
            + "WHERE order.status = 'POSTED'\n"
            + "RETURN order")
    List<Order> findPostedByOwnerName(@Param("name") String name);

    @Query("MATCH (:Owner {name: $name})-[selected: SELECTED]->(order)\n"
            + "WHERE order.status = 'POSTED'\n"
            + "AND selected.type = $type\n"
            + "RETURN order")
    List<Order> findSelectedByOwnerName(@Param("name") String name, @Param("type") SelectType selectType);

    @Query("MATCH p=(:Owner {name: $name})-[selected: SELECTED]->(order)-[*]-(:Owner {name: $name})\n"
            + "WHERE order.status = 'POSTED'\n"
            + "AND selected.type = $type\n"
            + "RETURN nodes(p)")
    List<ListValue> findMatchedByOwnerName(@Param("name") String name, @Param("type") SelectType selectType);
}
