package cz.tstrecha.timetracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.tstrecha.timetracker.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext implements Principal {

    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private ContextUserDTO loggedAs;

    private List<String> activePermissions; // What permissions has user in context to user in loggedAs

    @Override
    @JsonIgnore
    public String getName() {
        return fullName;
    }

    @JsonIgnore
    public Long getCurrentUserId() {
        return loggedAs.getId();
    }

    @JsonIgnore
    public Boolean isAccountOwner() {
        return Objects.equals(id, loggedAs.getId());
    }

    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(isAccountOwner()) {
            return List.of(new SimpleGrantedAuthority(STR."ROLE_\{getRole().name()}"), new SimpleGrantedAuthority("ROLE_ACCOUNT_OWNER"));
        } else {
            return List.of(new SimpleGrantedAuthority(STR."ROLE_\{getRole().name()}"));
        }
    }
}
