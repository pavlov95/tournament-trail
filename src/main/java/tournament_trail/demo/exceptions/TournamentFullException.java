package tournament_trail.demo.exceptions;

public class TournamentFullException extends IllegalStateException {
    public TournamentFullException() {
        super("This tournament if already full");
    }
}
