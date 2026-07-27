package tournament_trail.demo.exceptions;

public class InvalidReviewException extends RuntimeException {
    public InvalidReviewException() {
        super("No such review exists");
    }
}
