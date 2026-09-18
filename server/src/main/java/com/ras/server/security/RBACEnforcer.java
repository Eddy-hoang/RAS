package com.ras.server.security;

import com.ras.common.protocol.CommandType;

public class RBACEnforcer {

    public static boolean isAuthorized(Role role, CommandType commandType) {
        switch (commandType) {
            // VIEWER permissions (Read-Only)
            case SYSTEM_INFO_REQUEST:
            case PROCESS_LIST_REQUEST:
            case FILE_LIST_REQUEST:
            case HEARTBEAT_PING:
            case HEARTBEAT_PONG:
                return role.getLevel() >= Role.VIEWER.getLevel();

            // OPERATOR permissions (Read + Screen Stream + File Download)
            case SCREEN_START_REQUEST:
            case SCREEN_STOP_REQUEST:
            case FILE_DOWNLOAD_REQUEST:
                return role.getLevel() >= Role.OPERATOR.getLevel();

            // ADMIN permissions (Full control: Process Kill, Power, Delete)
            case PROCESS_KILL_REQUEST:
            case SYSTEM_POWER_ACTION:
            case FILE_DELETE_REQUEST:
            case FILE_UPLOAD_REQUEST:
                return role.getLevel() >= Role.ADMIN.getLevel();

            default:
                return false;
        }
    }
}
