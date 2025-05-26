package com.ppks.fernet.backend.domain.enumeration;

public enum Role {
    CLIENT,
    WORKER,
    ADMIN,
    ;
    public static Role fromString(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
