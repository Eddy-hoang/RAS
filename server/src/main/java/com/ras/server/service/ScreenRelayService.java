package com.ras.server.service;

import com.ras.common.protocol.Frame;
import com.ras.common.protocol.FrameCodec;
import com.ras.server.session.AgentSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ScreenRelayService {
    private static final Logger log = LoggerFactory.getLogger(ScreenRelayService.class);

    /**
     * Zero-copy pass-through relay of screen tile frames from Agent data channel to Admin Console data channel.
     */
    public void relayTileFrame(AgentSession agentSession, AgentSession adminSession, Frame tileFrame) throws IOException {
        if (adminSession != null && adminSession.getDataOutputStream() != null) {
            FrameCodec.writeFrame(adminSession.getDataOutputStream(), tileFrame);
            if (log.isDebugEnabled()) {
                log.debug("Relayed tile frame ({} bytes) from agent [{}] to admin [{}]",
                        tileFrame.getPayloadLength(), agentSession.getClientId(), adminSession.getClientId());
            }
        } else {
            log.warn("Cannot relay screen tile frame: Admin Console data channel not available");
        }
    }
}
