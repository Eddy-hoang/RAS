package com.ras.common.protocol;

public enum ErrorCode {
    AUTH_FAILED(401),
    UNAUTHORIZED(403),
    FORBIDDEN(403),
    PATH_TRAVERSAL_DETECTED(400),
    RESOURCE_NOT_FOUND(404),
    COMMAND_EXECUTION_FAILED(500),
    TIMEOUT(408),
    INTERNAL_ERROR(500);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
