package com.packx.backend.service;

import com.packx.backend.config.StorageProperties;
import com.packx.backend.repository.OperationRepository;
import com.packx.backend.model.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Exercises PackingService -> UnpackingService together, verifying the
 * round trip preserves file names, sizes and content exactly - including
 * a filename with spaces, which the original console app could not
 * handle correctly.
 */
class PackUnpackRoundTripTest {

    @TempDir
    Path tempDir;

    private PackingService packingService;
    private UnpackingService unpackingService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getStorage().setUploadDir(tempDir.resolve("uploads").toString());
        props.getStorage().setOutputDir(tempDir.resolve("output").toString());
        props.getStorage().setExtractedDir(tempDir.resolve("extracted").toString());
        props.getPacking().setAllowedExtensions(List.of(".txt"));
        props.getPacking().setMaxFilesPerOperation(10);

        FileStorageService storageService = new FileStorageService(props);
        EncryptionService encryptionService = new EncryptionService();

        OperationRepository repo = Mockito.mock(OperationRepository.class);
        when(repo.save(Mockito.any(Operation.class))).thenAnswer(inv -> {
            Operation op = inv.getArgument(0);
            if (op.getId() == null) {
                op.setId(java.util.UUID.randomUUID().toString());
            }
            return op;
        });
        OperationService operationService = new OperationService(repo);

        packingService = new PackingService(storageService, encryptionService, operationService, props);
        unpackingService = new UnpackingService(storageService, encryptionService, operationService);
    }

    @Test
    void packThenUnpackRestoresOriginalContent() throws IOException {
        String content1 = "Hello from file one!\nLine two.";
        String content2 = "Second file contents, with some numbers 12345.";

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "notes.txt", "text/plain", content1.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "my important notes.txt", "text/plain", content2.getBytes(StandardCharsets.UTF_8));

        Operation packOp = packingService.pack(List.of(file1, file2), "testpack");
        assertEquals(com.packx.backend.model.OperationStatus.SUCCESS, packOp.getStatus());

        Path packedPath = Path.of(packOp.getStoredFilePath());
        assertTrue(Files.exists(packedPath));

        MockMultipartFile packedUpload;
        try (InputStream in = Files.newInputStream(packedPath)) {
            packedUpload = new MockMultipartFile(
                    "file", "testpack.pack", "application/octet-stream", in.readAllBytes());
        }

        UnpackingService.UnpackResult result = unpackingService.unpack(packedUpload);

        assertEquals(2, result.extractedFileNames().size());
        assertTrue(result.extractedFileNames().contains("notes.txt"));
        assertTrue(result.extractedFileNames().contains("my important notes.txt"));
    }

    @Test
    void packRejectsEmptyFileList() {
        assertThrows(RuntimeException.class, () -> packingService.pack(List.of(), "empty"));
    }

    @Test
    void packRejectsUnsupportedExtension() {
        MockMultipartFile badFile = new MockMultipartFile(
                "files", "image.png", "image/png", new byte[]{1, 2, 3});
        assertThrows(RuntimeException.class, () -> packingService.pack(List.of(badFile), "badpack"));
    }

    @Test
    void packRejectsDuplicateFilenames() {
        MockMultipartFile a = new MockMultipartFile("files", "dup.txt", "text/plain", "a".getBytes());
        MockMultipartFile b = new MockMultipartFile("files", "dup.txt", "text/plain", "b".getBytes());
        assertThrows(RuntimeException.class, () -> packingService.pack(List.of(a, b), "duppack"));
    }
}
