package ge.elzhart.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TransactionGraphDto {

    private String ownerName;
    private String orderId;
    private String orderTitle;
}
