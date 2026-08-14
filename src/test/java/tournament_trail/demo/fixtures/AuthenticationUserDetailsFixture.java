package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.security.AuthenticationUserDetails;

import java.util.UUID;

public class AuthenticationUserDetailsFixture {
    public static final String TEST_USERNAME = "TestUsername1.";
    public static final String TEST_PASSWORD = "TestPassword1.";

    public static AuthenticationUserDetails create() {
        return AuthenticationUserDetails.builder()
                .id(UUID.randomUUID())
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .role(Role.PLAYER)
                .enabled(true)
                .build();
    }
    public static AuthenticationUserDetails createDisabled() {
        return AuthenticationUserDetails.builder()
                .id(UUID.randomUUID())
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .role(Role.PLAYER)
                .enabled(false)
                .build();
    }
}
