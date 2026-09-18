package com.ras.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final ConcurrentHashMap<String, AgentSession> sessionsByToken = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AgentSession> sessionsByClient = new ConcurrentHashMap<>();

    public AgentSession registerSession(String clientId, AgentSession.ClientType clientType) {
        AgentSession session = new AgentSession(clientId, clientType);
        sessionsByToken.put(session.getSessionToken(), session);
        sessionsByClient.put(clientId, session);
        log.info("Registered new session: {} for client [{}]", session.getSessionToken(), clientId);
        return session;
    }

    public AgentSession getSessionByToken(String token) {
        return sessionsByToken.get(token);
    }

    public AgentSession getSessionByClientId(String clientId) {
        return sessionsByClient.get(clientId);
    }

    public Collection<AgentSession> getAllSessions() {
        return Collections.unmodifiableCollection(sessionsByToken.values());
    }

    public void removeSession(String token) {
        AgentSession session = sessionsByToken.remove(token);
        if (session != null) {
            sessionsByClient.remove(session.getClientId());
            session.close();
            log.info("Closed and removed session: {}", token);
        }
    }

    public void checkStaleSessions(long timeoutMillis) {
        long now = System.currentTimeMillis();
        for (AgentSession session : sessionsByToken.values()) {
            if (now - session.getLastHeartbeat() > timeoutMillis) {
                if (session.getState() != AgentSession.SessionState.CLOSED) {
                    log.warn("Session [{}] for client [{}] timed out (no heartbeat for {} ms). Closing session.",
                            session.getSessionToken(), session.getClientId(), now - session.getLastHeartbeat());
                    session.setState(AgentSession.SessionState.STALE);
                    removeSession(session.getSessionToken());
                }
            }
        }
    }
}
