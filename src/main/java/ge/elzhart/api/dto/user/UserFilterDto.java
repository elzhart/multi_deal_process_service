package ge.elzhart.api.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserFilterDto {
    private String id;
    private String username;
    private String fullName;
}
