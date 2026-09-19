package com.ras.agent;

import com.ras.agent.command.CommandHandler;
import com.ras.agent.screen.ScreenCaptureService;
import com.ras.agent.security.CommandPolicyEnforcer;
import com.ras.agent.service.FileService;
import com.ras.agent.service.ProcessService;
import com.ras.agent.service.SystemService;
import com.ras.common.protocol.CommandType;
import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameCodec;
import com.ras.common.protocol.FrameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class AgentMain {
    private static final Logger log = LoggerFactory.getLogger(AgentMain.class);
    public static final String DEFAULT_SERVER_HOST = "localhost";
    public static final int DEFAULT_SERVER_PORT = 8090;

    public static void main(String[] args) {
        log.info("Starting SRAP Client Agent...");
        try {
            String clientId = InetAddress.getLocalHost().getHostName();
            String serverHost = args.length > 0 ? args[0] : DEFAULT_SERVER_HOST;
            int serverPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_SERVER_PORT;

            SystemService systemService = new SystemService();
            ProcessService processService = new ProcessService();
            FileService fileService = new FileService(".");
            ScreenCaptureService screenCaptureService;
            try {
                screenCaptureService = new ScreenCaptureService();
            } catch (Exception e) {
                log.warn("ScreenCaptureService initialization skipped: {}", e.getMessage());
                screenCaptureService = null;
            }
            CommandPolicyEnforcer policyEnforcer = new CommandPolicyEnforcer();

            CommandHandler commandHandler = new CommandHandler(
                    systemService, processService, fileService, screenCaptureService, policyEnforcer);

            log.info("Client Agent [{}] connecting to Server at {}:{}...", clientId, serverHost, serverPort);

            Socket socket = new Socket(serverHost, serverPort);
            log.info("Connected successfully to SRAP Server at {}:{}. Agent active.", serverHost, serverPort);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Send initial Handshake HELLO frame
            Frame helloFrame = new Frame(FrameType.CONTROL, CommandType.SESSION_HELLO, clientId.getBytes());
            FrameCodec.writeFrame(out, helloFrame);

            // Keep-alive Event Loop: Read incoming frames from Server & dispatch to CommandHandler
            while (!socket.isClosed()) {
                Frame requestFrame = FrameCodec.readFrame(in);
                log.debug("Agent received frame: {}", requestFrame);

                if (requestFrame.getFrameType() == FrameType.HEARTBEAT && requestFrame.getCommandType() == CommandType.HEARTBEAT_PING) {
                    Frame pongFrame = new Frame(FrameType.HEARTBEAT, CommandType.HEARTBEAT_PONG, new byte[0]);
                    synchronized (out) {
                        FrameCodec.writeFrame(out, pongFrame);
                    }
                } else {
                    Frame responseFrame = commandHandler.handleCommand(requestFrame, out);
                    if (responseFrame != null) {
                        synchronized (out) {
                            FrameCodec.writeFrame(out, responseFrame);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error running SRAP Client Agent: {}", e.getMessage());
        }
    }
}
