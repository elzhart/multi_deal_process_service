package ge.elzhart.model.domain;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Node
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Owner {

    @Id
    private String name;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String lastModifiedBy;
    @Relationship(type = "CREATED", direction = Relationship.Direction.OUTGOING)
    private List<Order> created;
    @Relationship(type = "SELECTED", direction = Relationship.Direction.OUTGOING)
    private List<SelectOrderProperties> selected;
}
