package com.ras.agent.service;

import com.ras.common.dto.ProcessInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessService {
    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);

    public List<ProcessInfoDTO> listProcesses() {
        List<ProcessInfoDTO> processList = new ArrayList<>();
        ProcessHandle.allProcesses().forEach(ph -> {
            try {
                long pid = ph.pid();
                ProcessHandle.Info info = ph.info();
                String command = info.command().orElse("N/A");
                String name = command.contains(java.io.File.separator) ? 
                        command.substring(command.lastIndexOf(java.io.File.separator) + 1) : command;
                String user = info.user().orElse("N/A");
                String startTime = info.startInstant().map(Object::toString).orElse("N/A");

                processList.add(new ProcessInfoDTO(pid, name, command, user, 0.0, 0, startTime));
            } catch (Exception ignored) {}
        });
        return processList;
    }

    public boolean killProcess(long pid) {
        Optional<ProcessHandle> phOpt = ProcessHandle.of(pid);
        if (phOpt.isPresent()) {
            ProcessHandle ph = phOpt.get();
            log.warn("Attempting to terminate process PID [{}]", pid);
            return ph.destroyForcibly();
        }
        log.warn("Process PID [{}] not found", pid);
        return false;
    }
}
