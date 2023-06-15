package ge.elzhart.api.dto.order;

import ge.elzhart.model.domain.order.OrderCategory;
import ge.elzhart.model.domain.order.OrderStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderFilterDto {
    private String title;
    private OrderStatus status;
    private OrderCategory category;
    private String country;
    private String username;
}
