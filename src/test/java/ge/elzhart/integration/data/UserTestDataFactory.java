package ge.elzhart.integration.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ge.elzhart.api.dto.user.CreateUserRequest;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.service.user.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Service
public class UserTestDataFactory {

    @Autowired
    private UserService userService;

    public UserDto createUser(String username,
            String fullName,
            String password) {
        CreateUserRequest createRequest = new CreateUserRequest(
                username, fullName, password, password
        );

        UserDto userDto = userService.create(createRequest);

        assertNotNull(userDto.id(), "User id must not be null!");
        assertEquals(fullName, userDto.fullName(), "User name update isn't applied!");

        return userDto;
    }

    public UserDto createUser(String username,
            String fullName) {
        return createUser(username, fullName, "Test12345_");
    }
}
