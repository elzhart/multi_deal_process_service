package ge.elzhart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ge.elzhart.api.dto.user.AuthRequest;
import ge.elzhart.api.dto.user.CreateUserRequest;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.integration.data.UserTestDataFactory;

import static ge.elzhart.util.JsonHelper.fromJson;
import static ge.elzhart.util.JsonHelper.toJson;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuthControllerTest extends AbstractIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserTestDataFactory userTestDataFactory;

    private final String password = "Test12345_";

    @Autowired
    public AuthControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserTestDataFactory userTestDataFactory) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userTestDataFactory = userTestDataFactory;
    }

    @Test
    public void testLoginSuccess() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User", password);

        AuthRequest request = new AuthRequest(userDto.username(), password);

        MvcResult createResult = this.mockMvc
                .perform(post("/api/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        UserDto authUserDto = fromJson(objectMapper, createResult.getResponse().getContentAsString(), UserDto.class);
        assertEquals(userDto.id(), authUserDto.id(), "User ids must match!");
    }

    @Test
    public void testLoginFail() throws Exception {
        UserDto userDto = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
                "Test User", password);

        AuthRequest request = new AuthRequest(userDto.username(), "zxc");

        this.mockMvc
                .perform(post("/api/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, request)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andReturn();
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        CreateUserRequest goodRequest = new CreateUserRequest(
                String.format("test.user.%d@nix.com", currentTimeMillis()),
                "Test User A",
                password
        );

        MvcResult createResult = this.mockMvc
                .perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, goodRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto userDto = fromJson(objectMapper, createResult.getResponse().getContentAsString(), UserDto.class);
        assertNotNull(userDto.id(), "User id must not be null!");
        assertEquals(goodRequest.fullName(), userDto.fullName(), "User fullname  update isn't applied!");
    }

    @Test
    public void testRegisterFail() throws Exception {
        CreateUserRequest badRequest = new CreateUserRequest(
                "invalid.username", "", ""
        );

        this.mockMvc
                .perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Method argument validation failed")));
    }
}
