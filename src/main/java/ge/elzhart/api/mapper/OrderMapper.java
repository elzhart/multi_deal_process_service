package ge.elzhart.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import ge.elzhart.api.dto.order.OrderDto;
import ge.elzhart.model.domain.order.Order;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderDto toDto(Order order);

    Order toNode(OrderDto dto);
}
