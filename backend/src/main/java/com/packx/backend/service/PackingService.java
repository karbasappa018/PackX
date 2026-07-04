package com.packx.backend.service;

import com.packx.backend.config.StorageProperties;
import com.packx.backend.exception.FileProcessingException;
import com.packx.backend.exception.UnsupportedFileTypeException;
import com.packx.backend.exception.ValidationException;
import com.packx.backend.model.Operation;
import com.packx.backend.model.OperationStatus;
import com.packx.backend.model.OperationType;
import com.packx.backend.util.PackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


@Service
public class PackingService {

    private static final Logger log = LoggerFactory.getLogger(PackingService.class);

    private final FileStorageService storageService;
    private final EncryptionService encryptionService;
    private final OperationService operationService;
    private final StorageProperties storageProperties;

    public PackingService(FileStorageService storageService,
                           EncryptionService encryptionService,
                           OperationService operationService,
                           StorageProperties storageProperties) {
        this.storageService = storageService;
        this.encryptionService = encryptionService;
        this.operationService = operationService;
        this.storageProperties = storageProperties;
    }

    public Operation pack(List<MultipartFile> files, String packName) {
        long startTime = System.currentTimeMillis();

        validateRequest(files, packName);

        String outputFileName = packName.endsWith(".pack") ? packName : packName + ".pack";
        Path outputPath = storageService.newOutputFile(outputFileName);

        long totalSize = 0;
        Operation operation = operationService.startOperation(OperationType.PACK, joinNames(files), outputFileName);

        try (OutputStream out = Files.newOutputStream(outputPath)) {
            byte[] buffer = new byte[PackFormat.BUFFER_SIZE];

            for (MultipartFile file : files) {
                String safeName = storageService.sanitizeFileNameOnly(file.getOriginalFilename());
                validateExtension(safeName);

                long fileSize = file.getSize();
                out.write(PackFormat.buildHeader(safeName, fileSize));

                try (InputStream in = file.getInputStream()) {
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        encryptionService.transform(buffer, read);
                        out.write(buffer, 0, read);
                    }
                }
                totalSize += fileSize;
            }
        } catch (IOException e) {
            storageService.deleteQuietly(outputPath);
            operationService.failOperation(operation, "Failed to write packed file: " + e.getMessage());
            throw new FileProcessingException("Failed to write packed file", e);
        }

        long processingTime = System.currentTimeMillis() - startTime;
        return operationService.completeOperation(
                operation, outputPath, files.size(), totalSize, processingTime);
    }

    private void validateRequest(List<MultipartFile> files, String packName) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("At least one file must be selected for packing");
        }
        if (packName == null || packName.isBlank()) {
            throw new ValidationException("A name for the packed file is required");
        }
        if (files.size() > storageProperties.getPacking().getMaxFilesPerOperation()) {
            throw new ValidationException("Too many files in a single operation (max "
                    + storageProperties.getPacking().getMaxFilesPerOperation() + ")");
        }

        Set<String> seenNames = new HashSet<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new ValidationException("Empty file uploaded: " + file.getOriginalFilename());
            }
            String safeName = storageService.sanitizeFileNameOnly(file.getOriginalFilename());
            if (!seenNames.add(safeName.toLowerCase(Locale.ROOT))) {
                throw new ValidationException("Duplicate filename in upload: " + safeName);
            }
        }
    }

    private void validateExtension(String fileName) {
        List<String> allowed = storageProperties.getPacking().getAllowedExtensions();
        boolean ok = allowed.stream()
                .anyMatch(ext -> fileName.toLowerCase(Locale.ROOT).endsWith(ext.toLowerCase(Locale.ROOT)));
        if (!ok) {
            throw new UnsupportedFileTypeException(
                    "Unsupported file type for \"" + fileName + "\". Allowed: " + allowed);
        }
    }

    private String joinNames(List<MultipartFile> files) {
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
