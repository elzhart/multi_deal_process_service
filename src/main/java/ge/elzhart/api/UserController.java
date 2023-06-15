package ge.elzhart.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import ge.elzhart.api.dto.user.CreateUserRequest;
import ge.elzhart.api.dto.user.UpdateUserRequest;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.api.dto.user.UserFilterDto;
import ge.elzhart.model.domain.user.Role;
import ge.elzhart.service.user.UserSearchService;
import ge.elzhart.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "api/admin/user")
@RolesAllowed(Role.USER_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Use for actions related to users")
public class UserController {

    private final UserService userService;
    private final UserSearchService userSearchService;

    @PostMapping
    @Operation(summary = "create new user by admin")
    public UserDto create(@RequestBody @Valid CreateUserRequest request, Principal principal) {
        return userService.create(request, principal.getName());
    }

    @PutMapping("{id}")
    @Operation(summary = "update user with a given identifier")
    public UserDto update(
            @Parameter(description = "id of user to be updated") @PathVariable String id,
            @RequestBody @Valid UpdateUserRequest request,
            Principal principal
    ) {
        return userService.update(id, request, principal.getName());
    }

    @DeleteMapping("{id}")
    @Operation(summary = "delete user with a given identifier")
    public UserDto delete(@Parameter(description = "id of user to be deleted") @PathVariable String id) {
        return userService.delete(id);
    }

    @GetMapping("{id}")
    @Operation(summary = "find user by identifier")
    public UserDto get(@Parameter(description = "id of user to be found") @PathVariable String id) {
        return userService.getUser(id);
    }

    @PostMapping("search")
    @Operation(summary = "get users with optional filters")
    @Parameters({
            @Parameter(name = "sort", hidden = true, example = "user.username"),
            @Parameter(name = "page", description = "Zero-based page index (0..N)", example = "0"),
            @Parameter(name = "size", description = "The size of the page to be returned", example = "10")
    })
    public Page<UserDto> search(@RequestBody UserFilterDto filter, Pageable pageable) {
        return userSearchService.searchByFilter(filter, pageable);
    }
}
