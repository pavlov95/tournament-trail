package tournament_trail.demo.exceptions;

public class TravelGroupFullException extends IllegalStateException {
    public TravelGroupFullException() {
        super("Travel group already full.");
    }
}
