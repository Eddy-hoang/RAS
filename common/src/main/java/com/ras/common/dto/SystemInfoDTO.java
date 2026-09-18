package com.ras.common.dto;

public class SystemInfoDTO {
    private String hostname;
    private String osName;
    private String osVersion;
    private String osArch;
    private int availableProcessors;
    private double cpuLoadPercentage;
    private long totalMemoryBytes;
    private long freeMemoryBytes;
    private long systemUptimeMs;

    public SystemInfoDTO() {}

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }

    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

    public String getOsArch() { return osArch; }
    public void setOsArch(String osArch) { this.osArch = osArch; }

    public int getAvailableProcessors() { return availableProcessors; }
    public void setAvailableProcessors(int availableProcessors) { this.availableProcessors = availableProcessors; }

    public double getCpuLoadPercentage() { return cpuLoadPercentage; }
    public void setCpuLoadPercentage(double cpuLoadPercentage) { this.cpuLoadPercentage = cpuLoadPercentage; }

    public long getTotalMemoryBytes() { return totalMemoryBytes; }
    public void setTotalMemoryBytes(long totalMemoryBytes) { this.totalMemoryBytes = totalMemoryBytes; }

    public long getFreeMemoryBytes() { return freeMemoryBytes; }
    public void setFreeMemoryBytes(long freeMemoryBytes) { this.freeMemoryBytes = freeMemoryBytes; }

    public long getSystemUptimeMs() { return systemUptimeMs; }
    public void setSystemUptimeMs(long systemUptimeMs) { this.systemUptimeMs = systemUptimeMs; }
}
