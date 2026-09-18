package com.ras.server.network;

import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameCodec;
import com.ras.server.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerListener {
    private static final Logger log = LoggerFactory.getLogger(ServerListener.class);

    private final int port;
    private final SessionManager sessionManager;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public ServerListener(int port, SessionManager sessionManager) {
        this.port = port;
        this.sessionManager = sessionManager;
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
        try {
            // Read initial handshake frame to determine if Control or Data channel
            Frame initialFrame = FrameCodec.readFrame(socket.getInputStream());
            log.debug("Received initial handshake frame: {}", initialFrame);

            // Dispatch to session handler based on frame content
            // (Auth / Session binding logic)

        } catch (Exception e) {
            log.error("Failed handling connection from {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
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
