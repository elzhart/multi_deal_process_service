package ge.elzhart.model.domain;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.Data;

@RelationshipProperties
@Data
public class SelectOrderProperties {

    @RelationshipId
    private Long id;
    private SelectType type;
    @TargetNode
    private final Order order;

    public SelectOrderProperties(SelectType type, Order order) {
        this.type = type;
        this.order = order;
    }
}