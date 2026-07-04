package com.packx.backend.controller;

import com.packx.backend.dto.OperationResponse;
import com.packx.backend.exception.FileProcessingException;
import com.packx.backend.exception.OperationNotFoundException;
import com.packx.backend.model.Operation;
import com.packx.backend.service.OperationService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping
    public List<OperationResponse> listOperations() {
        return operationService.listAll().stream().map(OperationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OperationResponse getOperation(@PathVariable String id) {
        return OperationResponse.from(operationService.getById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        Operation operation = operationService.getById(id);
        if (operation.getStoredFilePath() == null) {
            throw new OperationNotFoundException("No downloadable file for operation " + id);
        }
        Path path = Path.of(operation.getStoredFilePath());
        if (!Files.exists(path)) {
            throw new FileProcessingException("Stored file is missing from disk for operation " + id);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + operation.getOutputFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        operationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
