package com.ras.agent.service;

import com.ras.common.dto.SystemInfoDTO;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;

public class SystemService {

    public SystemInfoDTO collectSystemInfo() {
        SystemInfoDTO dto = new SystemInfoDTO();
        try {
            dto.setHostname(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            dto.setHostname("UNKNOWN-HOST");
        }

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        dto.setOsName(osBean.getName());
        dto.setOsVersion(osBean.getVersion());
        dto.setOsArch(osBean.getArch());
        dto.setAvailableProcessors(osBean.getAvailableProcessors());

        Runtime runtime = Runtime.getRuntime();
        dto.setTotalMemoryBytes(runtime.totalMemory());
        dto.setFreeMemoryBytes(runtime.freeMemory());
        dto.setCpuLoadPercentage(osBean.getSystemLoadAverage());
        dto.setSystemUptimeMs(ManagementFactory.getRuntimeMXBean().getUptime());

        return dto;
    }
}
