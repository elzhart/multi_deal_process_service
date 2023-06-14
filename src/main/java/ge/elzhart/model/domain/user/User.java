package ge.elzhart.model.domain.user;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.DynamicLabels;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ge.elzhart.model.domain.Order;
import ge.elzhart.model.domain.Role;
import ge.elzhart.model.domain.SelectOrderProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import static java.util.stream.Collectors.toSet;

@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class User implements UserDetails {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    private String username;

    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String lastModifiedBy;

    private boolean enabled = true;

    private String password;
    private String fullName;
    @Relationship(type = "CREATED", direction = Relationship.Direction.OUTGOING)
    private List<Order> created;
    @Relationship(type = "SELECTED", direction = Relationship.Direction.OUTGOING)
    private List<SelectOrderProperties> selected;

    @DynamicLabels
    private Set<String> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    public void setAuthorities(Set<Role> authorities) {
        if (authorities != null) {
            this.authorities = authorities.stream().map(Role::getAuthority).collect(toSet());
        } else {
            this.authorities = new HashSet<>();
        }
    }

    @Override
    public Set<Role> getAuthorities() {
        if (authorities != null) {
            return authorities.stream().map(Role::new).collect(toSet());
        }
        return new HashSet<>();
    }
}
