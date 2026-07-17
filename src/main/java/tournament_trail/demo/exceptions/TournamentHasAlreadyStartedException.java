package tournament_trail.demo.exceptions;

public class TournamentHasAlreadyStartedException extends RuntimeException {
    public TournamentHasAlreadyStartedException() {
        super("Tournament has already started");
    }
}
