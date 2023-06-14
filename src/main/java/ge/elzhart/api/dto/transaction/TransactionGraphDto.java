package ge.elzhart.api.dto.transaction;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TransactionGraphDto {

    private String username;
    private String orderId;
    private String orderTitle;
}
