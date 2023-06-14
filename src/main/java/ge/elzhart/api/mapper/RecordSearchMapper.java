package ge.elzhart.api.mapper;

import org.neo4j.driver.Value;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.api.dto.transaction.TransactionGraphDto;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.model.domain.OrderCategory;
import ge.elzhart.model.domain.OrderStatus;
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

    public UserDto fromRecordToUser(Value value) {
        return new UserDto(
                value.get("id").asString(),
                value.get("username").asString(),
                value.get("fullName").asString());
    }

    public TransactionGraphDto fromRecordToTransaction(Value value) {
        return new TransactionGraphDto()
                .setUsername(value.get("username").asString());
    }
}
