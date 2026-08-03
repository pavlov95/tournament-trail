package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.User;

import java.util.UUID;

public class UserFixture {
    public static User createUser(){
        return User.builder()
                .id(UUID.randomUUID())
                .build();
    }
}
