package tournament_trail.demo.exceptions;

public class InvalidCommentException extends RuntimeException {
    public InvalidCommentException() {
        super("Comment does not exist");
    }
}
