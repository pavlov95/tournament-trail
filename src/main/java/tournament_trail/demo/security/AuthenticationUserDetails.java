package tournament_trail.demo.security;


import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tournament_trail.demo.entities.Role;


import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class CustomUserDetails implements UserDetails {
    private UUID id;
    private String username;
    private String password;
    private Role role;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
