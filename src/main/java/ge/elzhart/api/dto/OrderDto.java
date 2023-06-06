package ge.elzhart.api.dto;

import java.time.LocalDateTime;

import ge.elzhart.model.OrderCategory;
import ge.elzhart.model.OrderStatus;
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
