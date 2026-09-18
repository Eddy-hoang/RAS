package com.ras.agent.service;

import com.ras.common.dto.FileItemDTO;
import com.ras.common.util.ChecksumUtil;
import com.ras.common.util.PathValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final int CHUNK_SIZE = 64 * 1024; // 64 KB

    private final String baseDir;

    public FileService(String baseDir) {
        this.baseDir = baseDir;
    }

    public List<FileItemDTO> listDirectory(String relativePath) throws IOException {
        Path targetPath = PathValidator.validatePath(baseDir, relativePath);
        List<FileItemDTO> items = new ArrayList<>();

        if (Files.exists(targetPath) && Files.isDirectory(targetPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetPath)) {
                for (Path entry : stream) {
                    items.add(new FileItemDTO(
                            entry.getFileName().toString(),
                            entry.toString(),
                            Files.isDirectory(entry),
                            Files.isRegularFile(entry) ? Files.size(entry) : 0L,
                            Files.getLastModifiedTime(entry).toMillis()
                    ));
                }
            }
        }
        return items;
    }

    public void streamFileDownload(String relativePath, OutputStream out) throws IOException {
        Path targetPath = PathValidator.validatePath(baseDir, relativePath);
        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new IOException("File not found or invalid: " + relativePath);
        }

        try (InputStream is = Files.newInputStream(targetPath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int read;
            while ((read = is.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }

    public String getFileChecksum(String relativePath) throws IOException {
        Path targetPath = PathValidator.validatePath(baseDir, relativePath);
        return ChecksumUtil.calculateFileSHA256(targetPath);
    }
}
