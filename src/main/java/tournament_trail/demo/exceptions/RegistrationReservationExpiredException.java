package tournament_trail.demo.exceptions;

public class RegistrationReservationExpiredException extends RuntimeException {
    public RegistrationReservationExpiredException() {
        super("Your 1 hour reservation deadline has expired");
    }
}
