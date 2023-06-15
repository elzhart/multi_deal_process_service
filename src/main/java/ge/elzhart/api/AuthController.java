package ge.elzhart.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import ge.elzhart.api.dto.user.AuthRequest;
import ge.elzhart.api.dto.user.CreateUserRequest;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.api.mapper.UserViewMapper;
import ge.elzhart.model.domain.user.User;
import ge.elzhart.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;


@RestController
@RequestMapping(path = "api/public")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Use for actions related to login and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserViewMapper userViewMapper;
    private final UserService userService;

    @PostMapping("login")
    @Operation(summary = "login with credentials - username and password")
    public ResponseEntity<UserDto> login(@RequestBody @Valid AuthRequest request) {
        try {
            var authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            var user = (User) authentication.getPrincipal();

            var now = Instant.now();
            var expiry = 36000L;

            var scope =
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(joining(" "));

            var claims =
                    JwtClaimsSet.builder()
                            .issuer("example.io")
                            .issuedAt(now)
                            .expiresAt(now.plusSeconds(expiry))
                            .subject(format("%s,%s", user.getId(), user.getUsername()))
                            .claim("roles", scope)
                            .build();

            var token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body(userViewMapper.toUserView(user));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("register")
    @Operation(summary = "registers a user on the platform")
    public UserDto register(@RequestBody @Valid CreateUserRequest request) {
        return userService.create(request, request.username());
    }
}
