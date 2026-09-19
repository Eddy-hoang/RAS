package com.ras.agent.service;

import com.ras.common.dto.ProcessInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessService {
    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);

    public List<ProcessInfoDTO> listProcesses() {
        List<ProcessInfoDTO> processList = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            try {
                Process p = new ProcessBuilder("tasklist", "/FO", "CSV", "/NH").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        String[] parts = line.split("\",\"");
                        if (parts.length >= 2) {
                            String name = parts[0].replace("\"", "").trim();
                            String pidStr = parts[1].replace("\"", "").trim();
                            try {
                                long pid = Long.parseLong(pidStr);
                                String user = ProcessHandle.of(pid)
                                        .flatMap(ph -> ph.info().user())
                                        .orElse("DESKTOP-USER");
                                processList.add(new ProcessInfoDTO(pid, name, name, user, 0.0, 0, "N/A"));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                if (!processList.isEmpty()) {
                    return processList;
                }
            } catch (Exception e) {
                log.warn("Failed tasklist execution: {}", e.getMessage());
            }
        }

        ProcessHandle.allProcesses().forEach(ph -> {
            try {
                long pid = ph.pid();
                ProcessHandle.Info info = ph.info();
                String command = info.command().orElse("");
                
                String name;
                if (!command.isEmpty()) {
                    int lastSep = Math.max(command.lastIndexOf('/'), command.lastIndexOf('\\'));
                    name = (lastSep >= 0) ? command.substring(lastSep + 1) : command;
                } else {
                    name = "Process-" + pid;
                }

                String user = info.user().orElse("SYSTEM");
                String startTime = info.startInstant().map(Object::toString).orElse("N/A");

                processList.add(new ProcessInfoDTO(pid, name, command, user, 0.0, 0, startTime));
            } catch (Exception ignored) {}
        });
        return processList;
    }

    public boolean killProcess(long pid) {
        log.warn("Attempting to terminate process PID [{}]", pid);
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Process p = new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid)).start();
                int exitCode = p.waitFor();
                log.info("taskkill exit code for PID [{}]: {}", pid, exitCode);
                if (exitCode == 0) return true;
            } else if (os.contains("linux") || os.contains("mac")) {
                Process p = new ProcessBuilder("kill", "-9", String.valueOf(pid)).start();
                int exitCode = p.waitFor();
                log.info("kill -9 exit code for PID [{}]: {}", pid, exitCode);
                if (exitCode == 0) return true;
            }
        } catch (Exception e) {
            log.warn("Process termination command failed for PID [{}]: {}", pid, e.getMessage());
        }

        Optional<ProcessHandle> phOpt = ProcessHandle.of(pid);
        if (phOpt.isPresent()) {
            ProcessHandle ph = phOpt.get();
            boolean killed = ph.destroyForcibly() || ph.destroy();
            log.info("ProcessHandle termination status for PID [{}]: {}", pid, killed);
            return killed;
        }
        log.warn("Process PID [{}] not found", pid);
        return false;
    }
}

