package tournament_trail.demo.exceptions;

public class InvalidTournamentRegistrationException extends RuntimeException {
    public InvalidTournamentRegistrationException() {
        super("No such tournament registration exists");
    }
}
