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
import ge.elzhart.model.domain.Role;
import ge.elzhart.service.user.UserSearchService;
import ge.elzhart.service.user.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "api/admin/user")
@RolesAllowed(Role.USER_ADMIN)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserSearchService userSearchService;

    @PostMapping
    public UserDto create(@RequestBody @Valid CreateUserRequest request, Principal principal) {
        return userService.create(request, principal.getName());
    }

    @PutMapping("{id}")
    public UserDto update(@PathVariable String id, @RequestBody @Valid UpdateUserRequest request, Principal principal) {
        return userService.update(id, request, principal.getName());
    }

    @DeleteMapping("{id}")
    public UserDto delete(@PathVariable String id) {
        return userService.delete(id);
    }

    @GetMapping("{id}")
    public UserDto get(@PathVariable String id) {
        return userService.getUser(id);
    }

    @PostMapping("search")
    public Page<UserDto> search(@RequestBody UserFilterDto filter, Pageable pageable) {
        return userSearchService.searchByFilter(filter, pageable);
    }
}
