package ge.elzhart.api.dto.order;

import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SelectOrdersRequest {
    private List<UUID> orderIds;
}
