package tournament_trail.demo.entities.enums;

import tournament_trail.demo.security.Permission;

import java.util.EnumSet;
import java.util.Set;

public enum Role {

    PLAYER(
            "Player",
            EnumSet.of(
                    Permission.TRAVEL_GROUP_CREATE,
                    Permission.COMMENT_CREATE
            )
    ),

    ORGANISER(
            "Organiser",
            EnumSet.of(
                    Permission.TRAVEL_GROUP_CREATE,
                    Permission.TRAVEL_GROUP_MANAGE_OWN,
                    Permission.COMMENT_CREATE,
                    Permission.TOURNAMENT_CREATE,
                    Permission.TOURNAMENT_EDIT_OWN,
                    Permission.REGISTRATION_MANAGE
            )
    ),

    ADMIN(
            "Admin",
            EnumSet.allOf(Permission.class)
    );

    private final String displayName;
    private final Set<Permission> permissions;

    Role(String displayName, Set<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = Set.copyOf(permissions);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}