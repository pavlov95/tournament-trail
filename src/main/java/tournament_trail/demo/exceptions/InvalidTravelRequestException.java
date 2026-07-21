package tournament_trail.demo.exceptions;

public class InvalidTravelRequestException extends RuntimeException {
    public InvalidTravelRequestException() {
        super("No such Travel Request exists");
    }
}
