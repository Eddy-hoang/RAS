package com.ras.common.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathValidator {

    /**
     * Validates that requested relative/absolute path stays strictly within rootDir.
     * Prevents path traversal attacks like "../../../etc/passwd" or "C:\Windows\System32".
     */
    public static Path validatePath(String rootDir, String targetPath) throws SecurityException, IOException {
        Path root = Paths.get(rootDir).toAbsolutePath().normalize();
        Path target = root.resolve(targetPath).toAbsolutePath().normalize();

        if (!target.startsWith(root)) {
            throw new SecurityException("Path traversal attempt blocked: Target path [" + 
                targetPath + "] resolves outside root directory [" + rootDir + "]");
        }

        return target;
    }
}
