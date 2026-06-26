package tournament_trail.demo.entities;

public enum TimeControl {
    BULLET("Bullet"),
    BLITZ("Blitz"),
    RAPID("Rapid"),
    CLASSICAL("Classical");

    private final String displayName;
    TimeControl(String displayName){
        this.displayName=displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
