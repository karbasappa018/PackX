package com.packx.backend.service;

import com.packx.backend.exception.FileProcessingException;
import com.packx.backend.exception.InvalidPackedFileException;
import com.packx.backend.exception.ValidationException;
import com.packx.backend.model.Operation;
import com.packx.backend.model.OperationType;
import com.packx.backend.util.PackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class UnpackingService {

    private static final Logger log = LoggerFactory.getLogger(UnpackingService.class);

    private final FileStorageService storageService;
    private final EncryptionService encryptionService;
    private final OperationService operationService;

    public UnpackingService(FileStorageService storageService,
                             EncryptionService encryptionService,
                             OperationService operationService) {
        this.storageService = storageService;
        this.encryptionService = encryptionService;
        this.operationService = operationService;
    }

    public UnpackResult unpack(MultipartFile packedFile) {
        long startTime = System.currentTimeMillis();

        if (packedFile == null || packedFile.isEmpty()) {
            throw new ValidationException("A packed file must be uploaded");
        }

        Path extractionDir = storageService.newExtractionDirectory();
        Operation operation = operationService.startOperation(
                OperationType.UNPACK, packedFile.getOriginalFilename(), null);

        List<String> extractedFileNames = new ArrayList<>();
        long totalSize = 0;

        try (InputStream rawIn = packedFile.getInputStream();
             BufferedInputStream in = new BufferedInputStream(rawIn, PackFormat.BUFFER_SIZE)) {

            byte[] headerBuffer = new byte[PackFormat.HEADER_SIZE];
            byte[] copyBuffer = new byte[PackFormat.BUFFER_SIZE];

            int headerBytesRead;
            while ((headerBytesRead = readHeaderOrEnd(in, headerBuffer)) != -1) {
                if (headerBytesRead != PackFormat.HEADER_SIZE) {
                    throw new InvalidPackedFileException(
                            "Corrupted packed file: incomplete header block (" + headerBytesRead + " bytes)");
                }

                PackFormat.ParsedHeader parsed = PackFormat.parseHeader(headerBuffer);
                Path targetFile = storageService.resolveSafely(extractionDir, parsed.fileName());

                long remaining = parsed.fileSize();
                try (OutputStream out = Files.newOutputStream(targetFile)) {
                    while (remaining > 0) {
                        int toRead = (int) Math.min(copyBuffer.length, remaining);
                        int read = readFullyChunk(in, copyBuffer, toRead);
                        encryptionService.transform(copyBuffer, read);
                        out.write(copyBuffer, 0, read);
                        remaining -= read;
                    }
                }

                extractedFileNames.add(parsed.fileName());
                totalSize += parsed.fileSize();
            }
        } catch (InvalidPackedFileException e) {
            operationService.failOperation(operation, e.getMessage());
            throw e;
        } catch (IOException e) {
            operationService.failOperation(operation, "I/O error while unpacking: " + e.getMessage());
            throw new FileProcessingException("I/O error while unpacking", e);
        }

        if (extractedFileNames.isEmpty()) {
            operationService.failOperation(operation, "Packed file contained no entries");
            throw new InvalidPackedFileException("Packed file is empty or malformed");
        }

        Path downloadable = extractedFileNames.size() == 1
                ? extractionDir.resolve(extractedFileNames.get(0))
                : zipDirectory(extractionDir, operation.getId());

        long processingTime = System.currentTimeMillis() - startTime;
        Operation completed = operationService.completeOperation(
                operation, downloadable, extractedFileNames.size(), totalSize, processingTime);
        completed.setFileNames(extractedFileNames);
        operationService.save(completed);

        return new UnpackResult(completed, downloadable, extractedFileNames);
    }

    /** Returns -1 cleanly at end-of-stream (no more headers), or the number of bytes read otherwise. */
    private int readHeaderOrEnd(InputStream in, byte[] headerBuffer) throws IOException {
        int firstByte = in.read();
        if (firstByte == -1) {
            return -1;
        }
        headerBuffer[0] = (byte) firstByte;
        int totalRead = 1;
        while (totalRead < headerBuffer.length) {
            int read = in.read(headerBuffer, totalRead, headerBuffer.length - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }
        return totalRead;
    }

    private int readFullyChunk(InputStream in, byte[] buffer, int length) throws IOException {
        int totalRead = 0;
        while (totalRead < length) {
            int read = in.read(buffer, totalRead, length - totalRead);
            if (read == -1) {
                throw new InvalidPackedFileException("Corrupted packed file: unexpected end of stream");
            }
            totalRead += read;
        }
        return totalRead;
    }

    private Path zipDirectory(Path directory, String operationId) {
        Path zipPath = storageService.outputDir().resolve(operationId + "_extracted.zip");
        try (OutputStream fos = Files.newOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            try (var stream = Files.list(directory)) {
                for (Path file : (Iterable<Path>) stream::iterator) {
                    zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to build ZIP archive of extracted files", e);
        }
        return zipPath;
    }

    public record UnpackResult(Operation operation, Path downloadablePath, List<String> extractedFileNames) {
    }
}
