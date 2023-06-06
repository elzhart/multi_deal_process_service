package ge.elzhart.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import ge.elzhart.model.Order;
import ge.elzhart.api.dto.OrderDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderDto toDto(Order order);
    Order toNode(OrderDto dto);
}
