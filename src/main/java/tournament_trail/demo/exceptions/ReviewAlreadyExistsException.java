package tournament_trail.demo.exceptions;

public class ReviewAlreadyExistsException extends IllegalStateException {
    public ReviewAlreadyExistsException() {
        super("You have already reviewed this tournament");
    }
}
