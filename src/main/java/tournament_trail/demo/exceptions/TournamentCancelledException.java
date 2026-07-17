package tournament_trail.demo.exceptions;

public class TournamentCancelledException extends RuntimeException {
    public TournamentCancelledException() {
        super("This tournament has been cancelled");
    }
}
