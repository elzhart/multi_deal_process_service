package ge.elzhart.api.dto;

import com.google.common.base.CaseFormat;

import lombok.Data;

@Data
public class SortDto {
    private String property;
    private Direction direction;

    private static String camelCasePattern = "([a-z]+[A-Z]+\\w+)+";

    public SortDto(String[] sortBy, String defaultProperty) {
        if (sortBy == null || sortBy.length == 0) {
            init(defaultProperty, "");

        } else if (sortBy.length == 1) {
            init(sortBy[0], "");
        } else {
            init(sortBy[0], sortBy[1]);
        }
    }

    public String toParam() {
        return this.property + " " + this.direction.name();
    }

    private void init(String property, String direction) {
        if (property.matches(camelCasePattern)) {
            this.property = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property);
        } else {
            this.property = property;
        }
        this.direction = direction.isEmpty() ? Direction.ASC : Direction.valueOf(direction.toUpperCase());
    }
}
