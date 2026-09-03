package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserFixture {
    public static final String TEST_USERNAME="TestUsername1";

    public static User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .build();
    }

    public static User createWithAllFields() {
        return User.builder()
                .role(Role.PLAYER)
                .username(TEST_USERNAME)
                .password("$2b$10$N9qo8uLOickgx2ZMRZoMyeZOZTZ27Cz8McCi1n5muM5bJd8qK51vK")
                .email("example@abv.bg")
                .enabled(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static User createWithAllFieldsAndRoleOrganiser() {
        return User.builder()
                .role(Role.ORGANISER)
                .username(TEST_USERNAME)
                .password("$2b$10$N9qo8uLOickgx2ZMRZoMyeZOZTZ27Cz8McCi1n5muM5bJd8qK51vK")
                .email("example@abv.bg")
                .enabled(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static User createWithAllFieldsWithDifferentUsernameAndEmail() {
        User user = createWithAllFields();
        String unique = UUID.randomUUID().toString().substring(0,5);
        user.setEmail("example"+unique+"gmail.com");
        user.setUsername(TEST_USERNAME+unique);

        return user;
    }

    public static User createEnabledUser(){
        return User.builder()
                .role(Role.PLAYER)
                .username(TEST_USERNAME)
                .password("$2b$10$N9qo8uLOickgx2ZMRZoMyeZOZTZ27Cz8McCi1n5muM5bJd8qK51vK")
                .email("example@abv.bg")
                .enabled(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static User createEnabledUser(String password){
        return User.builder()
                .role(Role.PLAYER)
                .username(TEST_USERNAME)
                .password(password)
                .email("example@abv.bg")
                .enabled(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static User createEnabledUserWithRoleOrganiser(String password){
        return User.builder()
                .role(Role.ORGANISER)
                .username(TEST_USERNAME)
                .password(password)
                .email("example@abv.bg")
                .enabled(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
