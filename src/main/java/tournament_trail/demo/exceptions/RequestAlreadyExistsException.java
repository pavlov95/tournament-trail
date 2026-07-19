package tournament_trail.demo.exceptions;

public class RequestAlreadyExistsException extends RuntimeException {
    public RequestAlreadyExistsException() {
        super("A request has already been sent.");
    }
}
