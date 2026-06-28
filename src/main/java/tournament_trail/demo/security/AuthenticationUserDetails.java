package tournament_trail.demo.security;


import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tournament_trail.demo.entities.enums.Role;


import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class AuthenticationUserDetails implements UserDetails {
    private UUID id;
    private String username;
    private String password;
    private Role role;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
