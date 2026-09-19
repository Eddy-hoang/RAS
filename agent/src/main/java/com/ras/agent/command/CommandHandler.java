package com.ras.agent.command;

import com.ras.agent.screen.ScreenCaptureService;
import com.ras.agent.security.CommandPolicyEnforcer;
import com.ras.agent.service.FileService;
import com.ras.agent.service.ProcessService;
import com.ras.agent.service.SystemService;
import com.ras.common.dto.FileItemDTO;
import com.ras.common.dto.ProcessInfoDTO;
import com.ras.common.dto.ScreenTileDTO;
import com.ras.common.dto.SystemInfoDTO;
import com.ras.common.protocol.CommandType;
import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameType;
import com.ras.common.serialization.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private final SystemService systemService;
    private final ProcessService processService;
    private final FileService fileService;
    private final ScreenCaptureService screenCaptureService;
    private final CommandPolicyEnforcer policyEnforcer;

    public CommandHandler(SystemService systemService, ProcessService processService,
                          FileService fileService, ScreenCaptureService screenCaptureService,
                          CommandPolicyEnforcer policyEnforcer) {
        this.systemService = systemService;
        this.processService = processService;
        this.fileService = fileService;
        this.screenCaptureService = screenCaptureService;
        this.policyEnforcer = policyEnforcer;
    }

    private volatile boolean isStreamThreadRunning = false;

    private void startScreenStreamThread(OutputStream outStream) {
        if (isStreamThreadRunning) return;
        isStreamThreadRunning = true;

        Thread.ofVirtual().start(() -> {
            log.info("Started Virtual Thread for Remote Screen Delta Stream");
            try {
                while (screenCaptureService != null && screenCaptureService.isStreaming()) {
                    List<ScreenTileDTO> tiles = screenCaptureService.captureDeltaFrame();
                    if (tiles != null && !tiles.isEmpty()) {
                        byte[] payload = JsonCodec.toJsonBytes(tiles);
                        Frame tileFrame = new Frame(FrameType.DATA, CommandType.SCREEN_TILE_DATA, payload);
                        synchronized (outStream) {
                            com.ras.common.protocol.FrameCodec.writeFrame(outStream, tileFrame);
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                log.warn("Screen stream loop ended: {}", e.getMessage());
            } finally {
                isStreamThreadRunning = false;
                log.info("Remote Screen stream loop stopped");
            }
        });
    }

    public Frame handleCommand(Frame requestFrame, OutputStream outStream) {
        CommandType cmdType = requestFrame.getCommandType();
        log.info("Agent processing command: {}", cmdType);

        if (!policyEnforcer.isCommandAllowed(cmdType)) {
            log.warn("Command [{}] BLOCKED by local Agent CommandPolicyEnforcer!", cmdType);
            return createErrorResponse("BLOCKED_BY_AGENT_POLICY", "Local agent policy denied command " + cmdType);
        }

        try {
            switch (cmdType) {
                case SYSTEM_INFO_REQUEST: {
                    SystemInfoDTO info = systemService.collectSystemInfo();
                    byte[] payload = JsonCodec.toJsonBytes(info);
                    return new Frame(FrameType.CONTROL, CommandType.SYSTEM_INFO_RESPONSE, payload);
                }

                case PROCESS_LIST_REQUEST: {
                    List<ProcessInfoDTO> processList = processService.listProcesses();
                    byte[] payload = JsonCodec.toJsonBytes(processList);
                    return new Frame(FrameType.CONTROL, CommandType.PROCESS_LIST_RESPONSE, payload);
                }

                case PROCESS_KILL_REQUEST: {
                    String json = new String(requestFrame.getPayload(), StandardCharsets.UTF_8);
                    Map map = JsonCodec.fromJson(json, Map.class);
                    long pid = Long.parseLong(map.get("pid").toString());
                    boolean success = processService.killProcess(pid);
                    byte[] payload = JsonCodec.toJsonBytes(Map.of("pid", pid, "success", success));
                    return new Frame(FrameType.CONTROL, CommandType.PROCESS_KILL_RESPONSE, payload);
                }

                case FILE_LIST_REQUEST: {
                    String json = new String(requestFrame.getPayload(), StandardCharsets.UTF_8);
                    String path = ".";
                    if (json != null && json.contains("path")) {
                        Map map = JsonCodec.fromJson(json, Map.class);
                        if (map.containsKey("path")) path = map.get("path").toString();
                    }
                    List<FileItemDTO> files = fileService.listDirectory(path);
                    byte[] payload = JsonCodec.toJsonBytes(files);
                    return new Frame(FrameType.CONTROL, CommandType.FILE_LIST_RESPONSE, payload);
                }

                case SCREEN_START_REQUEST: {
                    if (screenCaptureService != null) {
                        screenCaptureService.setStreaming(true);
                        startScreenStreamThread(outStream);
                        return new Frame(FrameType.CONTROL, CommandType.GENERIC_RESPONSE, "{\"status\":\"STREAMING_STARTED\"}".getBytes());
                    }
                    return createErrorResponse("SCREEN_UNAVAILABLE", "Screen capture not available");
                }

                case SCREEN_STOP_REQUEST: {
                    if (screenCaptureService != null) {
                        screenCaptureService.setStreaming(false);
                    }
                    return new Frame(FrameType.CONTROL, CommandType.GENERIC_RESPONSE, "{\"status\":\"STREAMING_STOPPED\"}".getBytes());
                }

                default:
                    return createErrorResponse("UNKNOWN_COMMAND", "Unsupported command opcode: " + cmdType);
            }
        } catch (Exception e) {
            log.error("Failed executing command [{}]", cmdType, e);
            return createErrorResponse("EXECUTION_FAILED", e.getMessage());
        }
    }

    private Frame createErrorResponse(String errCode, String message) {
        try {
            byte[] payload = JsonCodec.toJsonBytes(Map.of("error", errCode, "message", message));
            return new Frame(FrameType.CONTROL, CommandType.ERROR_RESPONSE, payload);
        } catch (Exception e) {
            return new Frame(FrameType.CONTROL, CommandType.ERROR_RESPONSE, message.getBytes());
        }
    }
}
