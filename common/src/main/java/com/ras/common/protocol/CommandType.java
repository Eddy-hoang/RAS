package com.ras.common.protocol;

public enum CommandType {
    // Auth & Session
    AUTH_REQUEST(0x0101),
    AUTH_RESPONSE(0x0102),
    SESSION_HELLO(0x0103),
    SESSION_BIND_DATA(0x0104),
    HEARTBEAT_PING(0x0105),
    HEARTBEAT_PONG(0x0106),

    // System Commands
    SYSTEM_INFO_REQUEST(0x0201),
    SYSTEM_INFO_RESPONSE(0x0202),
    SYSTEM_POWER_ACTION(0x0203),

    // Process Commands
    PROCESS_LIST_REQUEST(0x0301),
    PROCESS_LIST_RESPONSE(0x0302),
    PROCESS_KILL_REQUEST(0x0303),
    PROCESS_KILL_RESPONSE(0x0304),

    // File Commands
    FILE_LIST_REQUEST(0x0401),
    FILE_LIST_RESPONSE(0x0402),
    FILE_DOWNLOAD_REQUEST(0x0403),
    FILE_DOWNLOAD_META(0x0404),
    FILE_UPLOAD_REQUEST(0x0405),
    FILE_CHUNK_DATA(0x0406),
    FILE_CHUNK_ACK(0x0407),
    FILE_DELETE_REQUEST(0x0408),

    // Screen Streaming
    SCREEN_START_REQUEST(0x0501),
    SCREEN_STOP_REQUEST(0x0502),
    SCREEN_TILE_DATA(0x0503),

    // Error & Generic Response
    GENERIC_RESPONSE(0x0901),
    ERROR_RESPONSE(0x0999);

    private final int id;

    CommandType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CommandType fromId(int id) {
        for (CommandType cmd : values()) {
            if (cmd.id == id) {
                return cmd;
            }
        }
        throw new IllegalArgumentException("Unknown CommandType ID: " + id);
    }
}
