package com.ras.server.security;

public enum Role {
    VIEWER(1),
    OPERATOR(2),
    ADMIN(3);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
