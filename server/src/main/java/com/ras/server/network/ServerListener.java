package com.ras.server.network;

import com.ras.common.protocol.CommandType;
import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameCodec;
import com.ras.common.protocol.FrameType;
import com.ras.server.security.Role;
import com.ras.server.service.CommandDispatcher;
import com.ras.server.session.AgentSession;
import com.ras.server.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerListener {
    private static final Logger log = LoggerFactory.getLogger(ServerListener.class);

    private final int port;
    private final SessionManager sessionManager;
    private final CommandDispatcher commandDispatcher;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public ServerListener(int port, SessionManager sessionManager, CommandDispatcher commandDispatcher) {
        this.port = port;
        this.sessionManager = sessionManager;
        this.commandDispatcher = commandDispatcher;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        log.info("RAS Server Listener started on port {} using Java 24 Virtual Threads", port);

        virtualThreadExecutor.submit(() -> {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    log.info("New connection accepted from {}", socket.getRemoteSocketAddress());
                    virtualThreadExecutor.submit(() -> handleNewConnection(socket));
                } catch (IOException e) {
                    if (!running) break;
                    log.error("Error accepting connection", e);
                }
            }
        });
    }

    private void handleNewConnection(Socket socket) {
        AgentSession session = null;
        try {
            // Read initial handshake frame
            Frame initialFrame = FrameCodec.readFrame(socket.getInputStream());
            log.debug("Received initial handshake frame: {}", initialFrame);

            String clientId = "CLIENT-" + socket.getPort();
            AgentSession.ClientType clientType = AgentSession.ClientType.AGENT;

            if (initialFrame.getCommandType() == CommandType.SESSION_HELLO && initialFrame.getPayloadLength() > 0) {
                clientId = new String(initialFrame.getPayload(), StandardCharsets.UTF_8);
                if ("ADMIN-CONSOLE".equalsIgnoreCase(clientId)) {
                    clientType = AgentSession.ClientType.ADMIN_CONSOLE;
                }
            }

            session = sessionManager.registerSession(clientId, clientType);
            session.setControlSocket(socket);
            session.setState(AgentSession.SessionState.AUTHENTICATED);
            log.info("Client [{}] ({}) authenticated & registered. Session: {}", clientId, clientType, session.getSessionToken());

            // Read loop for incoming frames from this socket
            while (!socket.isClosed()) {
                Frame frame = FrameCodec.readFrame(socket.getInputStream());
                session.touchHeartbeat();
                
                if (frame.getFrameType() == FrameType.HEARTBEAT) {
                    if (frame.getCommandType() == CommandType.HEARTBEAT_PONG) {
                        log.debug("Received PONG heartbeat from {}", clientId);
                    }
                } else if (commandDispatcher != null) {
                    commandDispatcher.dispatchFrame(session, Role.ADMIN, frame);
                }
            }
        } catch (Exception e) {
            log.warn("Connection finished for {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
            if (session != null) {
                sessionManager.removeSession(session.getSessionToken());
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        virtualThreadExecutor.shutdown();
        log.info("RAS Server Listener stopped.");
    }
}
