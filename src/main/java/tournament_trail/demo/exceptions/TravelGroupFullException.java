package tournament_trail.demo.exceptions;

public class TravelGroupFullException extends RuntimeException {
    public TravelGroupFullException() {
        super("Travel group already full.");
    }
}
