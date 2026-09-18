package com.ras.common.protocol;

public enum FrameType {
    CONTROL(0x0001),
    DATA(0x0002),
    HEARTBEAT(0x0003),
    CLOSE(0x0004);

    private final int id;

    FrameType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static FrameType fromId(int id) {
        for (FrameType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown FrameType ID: " + id);
    }
}
