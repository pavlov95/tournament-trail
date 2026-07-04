package tournament_trail.demo.exceptions;

public class TournamentDoesNotExist extends RuntimeException {
    public TournamentDoesNotExist() {
        super("No such tournament exists");
    }
}
