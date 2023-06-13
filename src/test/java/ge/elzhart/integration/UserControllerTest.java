package ge.elzhart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ge.elzhart.api.dto.user.CreateUserRequest;
import ge.elzhart.api.dto.user.UpdateUserRequest;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.integration.data.UserTestDataFactory;
import ge.elzhart.model.domain.Role;

import static ge.elzhart.util.JsonHelper.fromJson;
import static ge.elzhart.util.JsonHelper.toJson;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@WithMockUser(roles = Role.USER_ADMIN)
public class UserControllerTest extends AbstractIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserTestDataFactory userTestDataFactory;

    @Autowired
    public UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserTestDataFactory userTestDataFactory) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userTestDataFactory = userTestDataFactory;
    }

    @Test
    public void testCreateSuccess() throws Exception {
        CreateUserRequest goodRequest = new CreateUserRequest(
                String.format("test.user.%d@nix.com", currentTimeMillis()),
                "Test User A",
                "Test12345_"
        );

        MvcResult createResult = this.mockMvc
                .perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, goodRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto userDto = fromJson(objectMapper, createResult.getResponse().getContentAsString(), UserDto.class);
        assertNotNull(userDto.id(), "User id must not be null!");
        assertEquals(goodRequest.fullName(), userDto.fullName(), "User fullname  update isn't applied!");
    }

    @Test
    public void testCreateFail() throws Exception {
        CreateUserRequest badRequest = new CreateUserRequest(
                "invalid.username", "", ""
        );

        this.mockMvc
                .perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Method argument validation failed")));
    }

    @Test
    public void testCreateUsernameExists() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User A");

        CreateUserRequest badRequest = new CreateUserRequest(
                userDto.username(),
                "Test User A",
                "Test12345_"
        );

        this.mockMvc
                .perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username exists")));
    }

    @Test
    public void testCreatePasswordsMismatch() throws Exception {
        CreateUserRequest badRequest = new CreateUserRequest(
                String.format("test.user.%d@nix.com", currentTimeMillis()),
                "Test User A",
                "Test12345_",
                "Test12345"
        );

        this.mockMvc
                .perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Passwords don't match")));
    }

    @Test
    public void testEditSuccess() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User A");

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Test User B", null
        );

        MvcResult updateResult = this.mockMvc
                .perform(put(String.format("/api/admin/user/%s", userDto.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        UserDto newUserDto = fromJson(objectMapper, updateResult.getResponse().getContentAsString(), UserDto.class);

        assertEquals(updateRequest.fullName(), newUserDto.fullName(), "User fullname update isn't applied!");
    }

    @Test
    public void testEditFailBadRequest() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User A");

        UpdateUserRequest updateRequest = new UpdateUserRequest();

        this.mockMvc
                .perform(put(String.format("/api/admin/user/%s", userDto.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testEditFailNotFound() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Test User B", null
        );

        this.mockMvc
                .perform(put(String.format("/api/admin/user/%s", "5f07c259ffb98843e36a2aa9"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entity User with id 5f07c259ffb98843e36a2aa9 not found")));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User A");

        this.mockMvc
                .perform(delete(String.format("/api/admin/user/%s", userDto.id())))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(get(String.format("/api/admin/user/%s", userDto.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteFailNotFound() throws Exception {
        this.mockMvc
                .perform(delete(String.format("/api/admin/user/%s", "5f07c259ffb98843e36a2aa9")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entity User with id 5f07c259ffb98843e36a2aa9 not found")));
    }

    @Test
    public void testDeleteAndCreateAgain() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User A");

        this.mockMvc
                .perform(delete(String.format("/api/admin/user/%s", userDto.id())))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(get(String.format("/api/admin/user/%s", userDto.id())))
                .andExpect(status().isNotFound());

        CreateUserRequest goodRequest = new CreateUserRequest(
                userDto.username(),
                "Test User A",
                "Test12345_"
        );

        MvcResult createResult = this.mockMvc
                .perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, goodRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto newUserDto = fromJson(objectMapper, createResult.getResponse().getContentAsString(), UserDto.class);
        assertNotEquals(userDto.id(), newUserDto.id(), "User ids must not match!");
        assertEquals(userDto.username(), newUserDto.username(), "User names must match!");
    }

    @Test
    public void testGetSuccess() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User A");

        MvcResult getResult = this.mockMvc
                .perform(get(String.format("/api/admin/user/%s", userDto.id())))
                .andExpect(status().isOk())
                .andReturn();

        UserDto newUserDto = fromJson(objectMapper, getResult.getResponse().getContentAsString(), UserDto.class);

        assertEquals(userDto.id(), newUserDto.id(), "User ids must be equal!");
    }

    @Test
    public void testGetNotFound() throws Exception {
        this.mockMvc
                .perform(get(String.format("/api/admin/user/%s", "5f07c259ffb98843e36a2aa9")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entity User with id 5f07c259ffb98843e36a2aa9 not found")));
    }

}
