package cz.tstrecha.timetracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.tstrecha.timetracker.constant.UserRole;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext implements Principal, UserDetails {

    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    @Nullable
    private ContextUserDTO loggedAs;

    private List<ContextUserDTO> relationshipsReceiving;

    private List<String> activePermissions; // What permissions has user in context to user in loggedAs

    @Override
    @JsonIgnore
    public String getName() {
        return loggedAs.getFullName();
    }


    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(getRole().name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return getEmail();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
