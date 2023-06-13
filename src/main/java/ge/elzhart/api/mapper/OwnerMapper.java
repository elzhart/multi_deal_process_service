package ge.elzhart.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import ge.elzhart.api.dto.owner.OwnerDto;
import ge.elzhart.model.domain.Owner;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OwnerMapper {

    OwnerDto toDto(Owner owner);
}
