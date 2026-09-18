package com.ras.common.dto;

public class ProcessInfoDTO {
    private long pid;
    private String name;
    private String command;
    private String user;
    private double cpuUsagePercent;
    private long memorySizeBytes;
    private String startTime;

    public ProcessInfoDTO() {}

    public ProcessInfoDTO(long pid, String name, String command, String user, double cpuUsagePercent, long memorySizeBytes, String startTime) {
        this.pid = pid;
        this.name = name;
        this.command = command;
        this.user = user;
        this.cpuUsagePercent = cpuUsagePercent;
        this.memorySizeBytes = memorySizeBytes;
        this.startTime = startTime;
    }

    public long getPid() { return pid; }
    public void setPid(long pid) { this.pid = pid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public double getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(double cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }

    public long getMemorySizeBytes() { return memorySizeBytes; }
    public void setMemorySizeBytes(long memorySizeBytes) { this.memorySizeBytes = memorySizeBytes; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
}
