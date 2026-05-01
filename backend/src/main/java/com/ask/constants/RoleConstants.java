package com.ask.constants;

/**
 * Role name constants matching the database role names.
 * Used for role checks throughout the application.
 */
public final class RoleConstants {

    private RoleConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String STATE_ADMIN = "STATE_ADMIN";
    public static final String DISTRICT_ADMIN = "DISTRICT_ADMIN";
    public static final String BLOCK_ADMIN = "BLOCK_ADMIN";
    public static final String RECEPTIONIST = "RECEPTIONIST";
    public static final String VOLUNTEER = "VOLUNTEER";
    public static final String PHARMACIST = "PHARMACIST";

    /** Hierarchy levels — lower number = higher authority */
    public static final int LEVEL_SUPER_ADMIN = 1;
    public static final int LEVEL_SYSTEM_ADMIN = 2;
    public static final int LEVEL_STATE_ADMIN = 3;
    public static final int LEVEL_DISTRICT_ADMIN = 4;
    public static final int LEVEL_BLOCK_ADMIN = 5;
    public static final int LEVEL_RECEPTIONIST = 6;
    public static final int LEVEL_VOLUNTEER = 7;
    public static final int LEVEL_PHARMACIST = 3; // Same authority level as State Admin for supply operations
}
