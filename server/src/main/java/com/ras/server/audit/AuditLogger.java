package com.ras.server.audit;

import com.ras.common.dto.AuditEntryDTO;
import com.ras.common.serialization.JsonCodec;
import com.ras.common.util.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuditLogger {
    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final Path auditFilePath;
    private final ExecutorService asyncWriter = Executors.newSingleThreadExecutor();
    private String lastHash = "GENESIS_HASH_00000000000000000000000000000000000000000000000000000000";

    public AuditLogger(String auditFilePathStr) {
        this.auditFilePath = Paths.get(auditFilePathStr).toAbsolutePath();
        try {
            if (auditFilePath.getParent() != null) {
                Files.createDirectories(auditFilePath.getParent());
            }
        } catch (IOException e) {
            log.error("Failed to create audit log directory", e);
        }
    }

    public synchronized void log(String adminUser, String clientId, String sessionId, String action, String target, String status, long durationMs) {
        AuditEntryDTO entry = new AuditEntryDTO();
        entry.setAdminUser(adminUser);
        entry.setClientId(clientId);
        entry.setSessionId(sessionId);
        entry.setAction(action);
        entry.setTarget(target);
        entry.setStatus(status);
        entry.setDurationMs(durationMs);

        // Compute Hash Chain
        entry.setPrevHash(lastHash);
        String rawData = String.format("%s|%d|%s|%s|%s|%s|%s|%s",
                lastHash, entry.getTimestamp(), adminUser, clientId, sessionId, action, target, status);
        String currentHash = ChecksumUtil.calculateSHA256(rawData.getBytes());
        entry.setHash(currentHash);
        this.lastHash = currentHash;

        asyncWriter.submit(() -> {
            try {
                String jsonLine = JsonCodec.toJsonString(entry) + System.lineSeparator();
                Files.writeString(auditFilePath, jsonLine, 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            } catch (IOException e) {
                log.error("Failed to write audit entry to file", e);
            }
        });
    }

    public void shutdown() {
        asyncWriter.shutdown();
    }
}
