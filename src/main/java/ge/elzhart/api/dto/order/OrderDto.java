package ge.elzhart.api.dto.order;

import java.time.LocalDateTime;

import ge.elzhart.model.domain.order.OrderCategory;
import ge.elzhart.model.domain.order.OrderStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderDto {
    private String id;
    private String title;
    private OrderStatus status;
    private String country;
    private OrderCategory category;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
