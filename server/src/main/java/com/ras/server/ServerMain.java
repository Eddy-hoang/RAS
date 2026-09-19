package com.ras.server;

import com.ras.server.audit.AuditLogger;
import com.ras.server.network.ServerListener;
import com.ras.server.service.CommandDispatcher;
import com.ras.server.service.HeartbeatService;
import com.ras.server.service.ScreenRelayService;
import com.ras.server.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);
    public static final int DEFAULT_PORT = 8090;

    public static void main(String[] args) {
        log.info("Starting SRAP Server (Secure Remote Administration Platform)...");
        try {
            SessionManager sessionManager = new SessionManager();
            AuditLogger auditLogger = new AuditLogger("logs/audit.log");
            ScreenRelayService screenRelayService = new ScreenRelayService();
            CommandDispatcher commandDispatcher = new CommandDispatcher(sessionManager, auditLogger, screenRelayService);
            HeartbeatService heartbeatService = new HeartbeatService(sessionManager, 15000);

            ServerListener listener = new ServerListener(DEFAULT_PORT, sessionManager, commandDispatcher);
            listener.start();
            heartbeatService.start();

            log.info("SRAP Server running successfully on port {}. Press Ctrl+C to stop.", DEFAULT_PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down SRAP Server...");
                listener.stop();
                heartbeatService.stop();
                auditLogger.shutdown();
            }));
        } catch (Exception e) {
            log.error("Fatal error starting SRAP Server", e);
        }
    }
}
