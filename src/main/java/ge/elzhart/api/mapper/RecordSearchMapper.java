package ge.elzhart.api.mapper;

import org.neo4j.driver.Value;

import ge.elzhart.api.dto.TransactionGraphDto;
import ge.elzhart.model.OrderCategory;
import ge.elzhart.model.OrderStatus;
import ge.elzhart.api.dto.OrderDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RecordSearchMapper {

    public OrderDto fromRecordToOrder(Value value) {
        return new OrderDto()
                .setId(value.get("id").asString())
                .setTitle(value.get("title").asString())
                .setCountry(value.get("country").asString())
                .setStatus(OrderStatus.valueOf(value.get("status").asString()))
                .setCategory(OrderCategory.valueOf(value.get("category").asString()));
    }

    public TransactionGraphDto fromRecordToTransaction(Value value) {
        return new TransactionGraphDto()
                .setOwnerName(value.get("name").asString());
    }
}
