package tournament_trail.demo.exceptions;

public class InvalidCommentContentException extends RuntimeException {
    public InvalidCommentContentException() {
        super("Comment content is invalid");
    }
}
