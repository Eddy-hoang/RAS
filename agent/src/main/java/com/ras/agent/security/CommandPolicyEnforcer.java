package com.ras.agent.security;

import com.ras.common.protocol.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

public class CommandPolicyEnforcer {
    private static final Logger log = LoggerFactory.getLogger(CommandPolicyEnforcer.class);

    private final Set<CommandType> allowedCommands = EnumSet.allOf(CommandType.class);
    private boolean allowRemotePowerControl = true;
    private boolean allowProcessKill = true;

    public boolean isCommandAllowed(CommandType commandType) {
        if (!allowedCommands.contains(commandType)) {
            log.warn("Command [{}] blocked by local agent policy (not in allowed set)", commandType);
            return false;
        }

        if (commandType == CommandType.SYSTEM_POWER_ACTION && !allowRemotePowerControl) {
            log.warn("Command [{}] blocked by local agent policy (power control disabled)", commandType);
            return false;
        }

        if (commandType == CommandType.PROCESS_KILL_REQUEST && !allowProcessKill) {
            log.warn("Command [{}] blocked by local agent policy (process kill disabled)", commandType);
            return false;
        }

        return true;
    }

    public void setAllowRemotePowerControl(boolean allow) {
        this.allowRemotePowerControl = allow;
    }

    public void setAllowProcessKill(boolean allow) {
        this.allowProcessKill = allow;
    }
}
