package com.ras.common.dto;

public class MessageHeader {
    private int version = 1;
    private String type;
    private String requestId;
    private String sessionId;
    private long timestamp = System.currentTimeMillis();
    private String status = "SUCCESS";
    private String errorCode;

    public MessageHeader() {}

    public MessageHeader(String type, String requestId, String sessionId) {
        this.type = type;
        this.requestId = requestId;
        this.sessionId = sessionId;
    }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
