package ge.elzhart.api.dto;

import ge.elzhart.model.OrderCategory;
import ge.elzhart.model.OrderStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderFilterDto {
    private String title;
    private OrderStatus status;
    private OrderCategory category;
    private String country;
}
