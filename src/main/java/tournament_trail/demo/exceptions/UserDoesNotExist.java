package tournament_trail.demo.exceptions;

public class UserDoesNotExist extends RuntimeException {
    public UserDoesNotExist() {
        super("No such user found");
    }
}
