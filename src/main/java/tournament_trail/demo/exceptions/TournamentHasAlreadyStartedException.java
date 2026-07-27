package tournament_trail.demo.exceptions;

public class TournamentHasAlreadyStartedException extends IllegalStateException {
    public TournamentHasAlreadyStartedException() {
        super("This action is not available because the tournament has already started.");
    }
}
