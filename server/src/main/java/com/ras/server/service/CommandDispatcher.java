package com.ras.server.service;

import com.ras.common.dto.MessageHeader;
import com.ras.common.protocol.CommandType;
import com.ras.common.protocol.ErrorCode;
import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameCodec;
import com.ras.common.protocol.FrameType;
import com.ras.common.serialization.JsonCodec;
import com.ras.server.audit.AuditLogger;
import com.ras.server.security.RBACEnforcer;
import com.ras.server.security.Role;
import com.ras.server.session.AgentSession;
import com.ras.server.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final SessionManager sessionManager;
    private final AuditLogger auditLogger;
    private final ScreenRelayService screenRelayService;

    public CommandDispatcher(SessionManager sessionManager, AuditLogger auditLogger, ScreenRelayService screenRelayService) {
        this.sessionManager = sessionManager;
        this.auditLogger = auditLogger;
        this.screenRelayService = screenRelayService;
    }

    public void dispatchFrame(AgentSession senderSession, Role callerRole, Frame frame) {
        long startTime = System.currentTimeMillis();
        CommandType cmdType = frame.getCommandType();
        String callerId = senderSession != null ? senderSession.getClientId() : "UNKNOWN";

        log.info("Dispatching frame [{}] from [{}] ({})", cmdType, callerId, senderSession != null ? senderSession.getClientType() : "N/A");

        if (senderSession != null && senderSession.getClientType() == AgentSession.ClientType.ADMIN_CONSOLE) {
            // Processing Request originating from Admin Console
            if (!RBACEnforcer.isAuthorized(callerRole, cmdType)) {
                log.warn("Access DENIED for Admin [{}] attempting [{}]", callerId, cmdType);
                auditLogger.log(callerId, "N/A", senderSession.getSessionToken(), cmdType.name(), "N/A", "BLOCKED_RBAC", System.currentTimeMillis() - startTime);
                sendFrameQuietly(senderSession, createErrorFrame(ErrorCode.FORBIDDEN, "Access Denied"));
                return;
            }

            if (cmdType == CommandType.SYSTEM_INFO_REQUEST && frame.getPayloadLength() == 0) {
                // Client List Request
                sendFrameQuietly(senderSession, handleClientListRequest(callerId, senderSession, startTime));
                return;
            }

            // Target Agent RPC Routing
            AgentSession targetAgent = findTargetAgent(frame);
            if (targetAgent == null || targetAgent.getControlOutputStream() == null) {
                log.warn("No active Target Agent found for command [{}]", cmdType);
                auditLogger.log(callerId, "UNKNOWN", senderSession.getSessionToken(), cmdType.name(), "N/A", "AGENT_NOT_FOUND", System.currentTimeMillis() - startTime);
                sendFrameQuietly(senderSession, createErrorFrame(ErrorCode.RESOURCE_NOT_FOUND, "No active target Agent connected"));
                return;
            }

            try {
                FrameCodec.writeFrame(targetAgent.getControlOutputStream(), frame);
                log.info("Relayed request [{}] from Admin to Agent [{}]", cmdType, targetAgent.getClientId());
                auditLogger.log(callerId, targetAgent.getClientId(), senderSession.getSessionToken(), cmdType.name(), "Relayed", "SUCCESS", System.currentTimeMillis() - startTime);
            } catch (IOException e) {
                log.error("Failed to forward request to Agent [{}]", targetAgent.getClientId(), e);
                sendFrameQuietly(senderSession, createErrorFrame(ErrorCode.COMMAND_EXECUTION_FAILED, "Failed forwarding to Agent: " + e.getMessage()));
            }
        } else if (senderSession != null && senderSession.getClientType() == AgentSession.ClientType.AGENT) {
            // Processing Response originating from Agent -> Relay back to Admin Consoles
            for (AgentSession adminSession : sessionManager.getAllSessions()) {
                if (adminSession.getClientType() == AgentSession.ClientType.ADMIN_CONSOLE && adminSession.getControlOutputStream() != null) {
                    try {
                        FrameCodec.writeFrame(adminSession.getControlOutputStream(), frame);
                        log.info("Relayed response [{}] from Agent [{}] to Admin [{}]", cmdType, callerId, adminSession.getClientId());
                    } catch (IOException e) {
                        log.error("Failed relaying response to Admin Console", e);
                    }
                }
            }
        }
    }

    private AgentSession findTargetAgent(Frame frame) {
        if (frame.getPayloadLength() > 0) {
            try {
                String json = new String(frame.getPayload(), StandardCharsets.UTF_8);
                if (json.contains("clientId")) {
                    Map map = JsonCodec.fromJson(json, Map.class);
                    if (map.containsKey("clientId")) {
                        String clientId = map.get("clientId").toString();
                        AgentSession s = sessionManager.getSessionByClientId(clientId);
                        if (s != null) return s;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Default to first connected AGENT
        return sessionManager.getAllSessions().stream()
                .filter(s -> s.getClientType() == AgentSession.ClientType.AGENT)
                .findFirst().orElse(null);
    }

    private Frame handleClientListRequest(String adminUser, AgentSession callerSession, long startTime) {
        try {
            List<Map<String, Object>> clientList = new ArrayList<>();
            for (AgentSession s : sessionManager.getAllSessions()) {
                if (s.getClientType() == AgentSession.ClientType.AGENT) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("clientId", s.getClientId());
                    map.put("sessionToken", s.getSessionToken());
                    map.put("state", s.getState().name());
                    map.put("ipAddress", s.getControlSocket() != null ? s.getControlSocket().getInetAddress().getHostAddress() : "127.0.0.1");
                    clientList.add(map);
                }
            }

            byte[] payload = JsonCodec.toJsonBytes(clientList);
            auditLogger.log(adminUser, "ALL", callerSession != null ? callerSession.getSessionToken() : "N/A",
                    "CLIENT_LIST_REQUEST", "Count: " + clientList.size(), "SUCCESS", System.currentTimeMillis() - startTime);

            return new Frame(FrameType.CONTROL, CommandType.SYSTEM_INFO_RESPONSE, payload);
        } catch (Exception e) {
            return createErrorFrame(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    private void sendFrameQuietly(AgentSession session, Frame frame) {
        if (session != null && session.getControlOutputStream() != null) {
            try {
                FrameCodec.writeFrame(session.getControlOutputStream(), frame);
            } catch (IOException e) {
                log.error("Failed sending frame quietly to [{}]", session.getClientId(), e);
            }
        }
    }

    private Frame createErrorFrame(ErrorCode code, String message) {
        try {
            Map<String, String> errMap = Map.of("error", code.name(), "message", message);
            byte[] payload = JsonCodec.toJsonBytes(errMap);
            return new Frame(FrameType.CONTROL, CommandType.ERROR_RESPONSE, payload);
        } catch (Exception e) {
            return new Frame(FrameType.CONTROL, CommandType.ERROR_RESPONSE, message.getBytes(StandardCharsets.UTF_8));
        }
    }
}
