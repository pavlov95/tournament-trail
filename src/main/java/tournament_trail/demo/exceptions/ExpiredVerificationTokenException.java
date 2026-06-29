package tournament_trail.demo.exceptions;

public class ExpiredVerificationTokenException extends RuntimeException {
    public ExpiredVerificationTokenException() {
        super("VerificationToken has expired");
    }
}
