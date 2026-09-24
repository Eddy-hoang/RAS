package com.ras.server.service;

import com.ras.common.protocol.CommandType;
import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameCodec;
import com.ras.common.protocol.FrameType;
import com.ras.server.session.AgentSession;
import com.ras.server.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatService {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final SessionManager sessionManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final long timeoutMillis;

    public HeartbeatService(SessionManager sessionManager, long timeoutMillis) {
        this.sessionManager = sessionManager;
        this.timeoutMillis = timeoutMillis;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Send Ping to all active sessions
                Frame pingFrame = new Frame(FrameType.HEARTBEAT, CommandType.HEARTBEAT_PING, new byte[0]);
                for (AgentSession session : sessionManager.getAllSessions()) {
                    if (session.getState() == AgentSession.SessionState.AUTHENTICATED || 
                        session.getState() == AgentSession.SessionState.ACTIVE) {
                        try {
                            if (session.getControlOutputStream() != null) {
                                FrameCodec.writeFrame(session.getControlOutputStream(), pingFrame);
                            }
                        } catch (Exception e) {
                            log.warn("Failed sending heartbeat ping to session [{}]: {}. Removing session.", session.getSessionToken(), e.getMessage());
                            sessionManager.removeSession(session.getSessionToken());
                        }
                    }
                }

                // Check for stale sessions
                sessionManager.checkStaleSessions(timeoutMillis);
            } catch (Exception e) {
                log.error("Error running heartbeat check", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
        log.info("HeartbeatService started (interval: 5s, timeout: {}ms)", timeoutMillis);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
