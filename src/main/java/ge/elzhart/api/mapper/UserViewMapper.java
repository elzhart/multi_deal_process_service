package ge.elzhart.api.mapper;

import org.mapstruct.Mapper;

import java.util.List;

import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.model.domain.user.User;


@Mapper(componentModel = "spring")
public abstract class UserViewMapper {

    public abstract UserDto toUserView(User user);

    public abstract List<UserDto> toUserView(List<User> users);

}
