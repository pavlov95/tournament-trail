package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.User;

import java.util.UUID;

public class UserFixture {
    public static User createUserWithRandomUUID(){
        return User.builder()
                .id(UUID.randomUUID())
                .build();
    }
    public static User createUser(UUID id){
        return User.builder()
                .id(id)
                .build();
    }
}
