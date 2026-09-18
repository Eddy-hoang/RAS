package com.ras.server.session;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AgentSession {

    public enum SessionState {
        CONNECTING,
        AUTHENTICATED,
        ACTIVE,
        STALE,
        CLOSED
    }

    public enum ClientType {
        AGENT,
        ADMIN_CONSOLE
    }

    private final String sessionToken;
    private final String clientId;
    private final ClientType clientType;
    private final long createdAt;
    
    private Socket controlSocket;
    private InputStream controlInputStream;
    private OutputStream controlOutputStream;

    private Socket dataSocket;
    private InputStream dataInputStream;
    private OutputStream dataOutputStream;

    private volatile SessionState state;
    private final AtomicLong lastHeartbeat;

    public AgentSession(String clientId, ClientType clientType) {
        this.sessionToken = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.clientType = clientType;
        this.createdAt = System.currentTimeMillis();
        this.state = SessionState.CONNECTING;
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
    }

    public synchronized void setControlSocket(Socket socket) throws Exception {
        this.controlSocket = socket;
        this.controlInputStream = socket.getInputStream();
        this.controlOutputStream = socket.getOutputStream();
    }

    public synchronized void setDataSocket(Socket socket) throws Exception {
        this.dataSocket = socket;
        this.dataInputStream = socket.getInputStream();
        this.dataOutputStream = socket.getOutputStream();
    }

    public String getSessionToken() { return sessionToken; }
    public String getClientId() { return clientId; }
    public ClientType getClientType() { return clientType; }
    public long getCreatedAt() { return createdAt; }

    public Socket getControlSocket() { return controlSocket; }
    public InputStream getControlInputStream() { return controlInputStream; }
    public OutputStream getControlOutputStream() { return controlOutputStream; }

    public Socket getDataSocket() { return dataSocket; }
    public InputStream getDataInputStream() { return dataInputStream; }
    public OutputStream getDataOutputStream() { return dataOutputStream; }

    public SessionState getState() { return state; }
    public void setState(SessionState state) { this.state = state; }

    public long getLastHeartbeat() { return lastHeartbeat.get(); }
    public void touchHeartbeat() { this.lastHeartbeat.set(System.currentTimeMillis()); }

    public synchronized void close() {
        this.state = SessionState.CLOSED;
        try { if (controlSocket != null && !controlSocket.isClosed()) controlSocket.close(); } catch (Exception ignored) {}
        try { if (dataSocket != null && !dataSocket.isClosed()) dataSocket.close(); } catch (Exception ignored) {}
    }

    @Override
    public String toString() {
        return "AgentSession{" +
                "clientId='" + clientId + '\'' +
                ", token='" + sessionToken + '\'' +
                ", type=" + clientType +
                ", state=" + state +
                '}';
    }
}
