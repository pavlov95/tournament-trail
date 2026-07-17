package tournament_trail.demo.exceptions;

public class AlreadyRegisteredException extends RuntimeException {
    public AlreadyRegisteredException() {
        super("You have already registered for this tournament");
    }
}
