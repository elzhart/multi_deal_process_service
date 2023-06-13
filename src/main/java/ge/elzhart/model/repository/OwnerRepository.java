package ge.elzhart.model.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ge.elzhart.model.domain.Owner;

public interface OwnerRepository extends Neo4jRepository<Owner, String> {

    Optional<Owner> findByName(@Param("name") String name);

    List<Owner> findAllByNameIn(@Param("names") Set<String> names);

    @Query("MATCH(:Owner {name: $name})-[c:CREATED]->(:Order{id: $id}) DELETE c")
    void detachOrderFromOwner(@Param("name") String ownerName, @Param("id") String orderId);
}
