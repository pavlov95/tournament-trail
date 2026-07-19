package tournament_trail.demo.exceptions;

public class AlreadyPartOfGroupException extends RuntimeException {
    public AlreadyPartOfGroupException() {
        super("You are already part of this group");
    }
}
