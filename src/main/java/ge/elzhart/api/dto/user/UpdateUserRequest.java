package ge.elzhart.api.dto.user;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public record UpdateUserRequest(
        @NotBlank
        String fullName,
        Set<String> authorities
) {

    @Builder
    public UpdateUserRequest {
    }

    public UpdateUserRequest() {
        this(null, null);
    }
}
