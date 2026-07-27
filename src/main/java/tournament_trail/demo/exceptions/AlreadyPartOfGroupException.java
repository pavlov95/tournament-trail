package tournament_trail.demo.exceptions;

public class AlreadyPartOfGroupException extends IllegalStateException {
    public AlreadyPartOfGroupException() {
        super("You cannot request to join your own travel group.");
    }
}
