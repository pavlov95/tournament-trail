package tournament_trail.demo.exceptions;

public class TournamentNotStartedException extends IllegalStateException {
    public TournamentNotStartedException() {
        super("You can not give a review to a tournament that has not even started");
    }
}
