package ge.elzhart.api.dto.owner;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OwnerDto {
    private String name;
    private LocalDateTime createdDate;
}
