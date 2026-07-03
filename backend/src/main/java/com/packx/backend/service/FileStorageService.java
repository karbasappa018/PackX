package com.packx.backend.service;

import com.packx.backend.config.StorageProperties;
import com.packx.backend.exception.FileProcessingException;
import com.packx.backend.exception.PathTraversalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadDir;
    private final Path outputDir;
    private final Path extractedDir;

    public FileStorageService(StorageProperties properties) {
        this.uploadDir = initDir(properties.getStorage().getUploadDir());
        this.outputDir = initDir(properties.getStorage().getOutputDir());
        this.extractedDir = initDir(properties.getStorage().getExtractedDir());
    }

    private Path initDir(String configured) {
        try {
            Path path = Paths.get(configured).toAbsolutePath().normalize();
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new FileProcessingException("Could not initialize storage directory: " + configured, e);
        }
    }

    public Path uploadDir() {
        return uploadDir;
    }

    public Path outputDir() {
        return outputDir;
    }

    public Path extractedDir() {
        return extractedDir;
    }


    public Path newExtractionDirectory() {
        Path dir = extractedDir.resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new FileProcessingException("Could not create extraction directory", e);
        }
        return dir;
    }

    public Path newOutputFile(String suggestedName) {
        String safeName = sanitizeFileNameOnly(suggestedName);
        String unique = UUID.randomUUID().toString().substring(0, 8) + "_" + safeName;
        return outputDir.resolve(unique);
    }


    public Path resolveSafely(Path baseDir, String rawFileName) {
        String safeName = sanitizeFileNameOnly(rawFileName);
        Path resolved = baseDir.resolve(safeName).normalize();
        Path normalizedBase = baseDir.normalize();
        if (!resolved.startsWith(normalizedBase)) {
            log.error("Blocked path traversal attempt with filename: {}", rawFileName);
            throw new PathTraversalException("Filename resolves outside the target directory: " + rawFileName);
        }
        return resolved;
    }


    public String sanitizeFileNameOnly(String rawFileName) {
        if (rawFileName == null || rawFileName.isBlank()) {
            throw new PathTraversalException("Filename must not be empty");
        }

        String name = rawFileName.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        name = name.trim();
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            throw new PathTraversalException("Invalid filename: " + rawFileName);
        }

        for (char c : name.toCharArray()) {
            if (c == '\0' || Character.isISOControl(c)) {
                throw new PathTraversalException("Filename contains illegal characters: " + rawFileName);
            }
        }
        return name;
    }

    public void copyInto(InputStream in, Path destination) {
        try (OutputStream out = Files.newOutputStream(destination)) {
            in.transferTo(out);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to store uploaded file", e);
        }
    }

    public void moveToOutput(Path tempFile, Path destination) {
        try {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to finalize output file", e);
        }
    }

    public void deleteQuietly(Path path) {
        try {
            if (path != null && Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            log.warn("Could not delete temporary file {}: {}", path, e.getMessage());
        }
    }
}
