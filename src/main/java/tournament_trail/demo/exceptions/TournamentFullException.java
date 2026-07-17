package tournament_trail.demo.exceptions;

public class TournamentFullException extends RuntimeException {
    public TournamentFullException() {
        super("This tournament if already full");
    }
}
