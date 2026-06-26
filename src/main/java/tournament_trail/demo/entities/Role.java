package tournament_trail.demo.entities;

public enum Role {
    PLAYER("Player"),
    ORGANISER("Organiser"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
