package tournament_trail.demo.exceptions;

public class TravelGroupDoesNotExistException extends RuntimeException {
    public TravelGroupDoesNotExistException() {
        super("No such travel group was found");
    }
}
