package com.projects.bills.Enums;

public enum LastAction {
    USER_RECYCLE,
    SYSTEM_RECYCLE,
    NONE;

    public static LastAction fromDb(String dbValue) {
        if (dbValue == null) return NONE;
        return switch (dbValue) {
            case "User_Recycle" -> USER_RECYCLE;
            case "System_Recycle" -> SYSTEM_RECYCLE;
            default -> NONE;
        };
    }

    public String toDb() {
        return switch (this) {
            case USER_RECYCLE -> "User_Recycle";
            case SYSTEM_RECYCLE -> "System_Recycle";
            case NONE -> null;
        };
    }
}
