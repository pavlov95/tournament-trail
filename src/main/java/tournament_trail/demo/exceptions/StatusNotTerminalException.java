package tournament_trail.demo.exceptions;

public class StatusNotTerminalException extends RuntimeException {
    public StatusNotTerminalException() {
        super("You are not allowed to hide this");
    }
}
