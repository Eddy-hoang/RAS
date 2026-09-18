package com.ras.common.dto;

public class FileItemDTO {
    private String name;
    private String path;
    private boolean isDirectory;
    private long sizeBytes;
    private long lastModifiedMs;

    public FileItemDTO() {}

    public FileItemDTO(String name, String path, boolean isDirectory, long sizeBytes, long lastModifiedMs) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.sizeBytes = sizeBytes;
        this.lastModifiedMs = lastModifiedMs;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public long getLastModifiedMs() { return lastModifiedMs; }
    public void setLastModifiedMs(long lastModifiedMs) { this.lastModifiedMs = lastModifiedMs; }
}
