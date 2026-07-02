package com.packx.backend.dto;

import com.packx.backend.model.Operation;
import com.packx.backend.model.OperationStatus;
import com.packx.backend.model.OperationType;

import java.time.Instant;
import java.util.List;


public class OperationResponse {

    private String id;
    private OperationType operationType;
    private String inputFileName;
    private String outputFileName;
    private int numberOfFiles;
    private long totalSize;
    private OperationStatus status;
    private String errorMessage;
    private List<String> fileNames;
    private Instant createdAt;
    private Instant completedAt;
    private Long processingTimeMs;

    public static OperationResponse from(Operation op) {
        OperationResponse dto = new OperationResponse();
        dto.id = op.getId();
        dto.operationType = op.getOperationType();
        dto.inputFileName = op.getInputFileName();
        dto.outputFileName = op.getOutputFileName();
        dto.numberOfFiles = op.getNumberOfFiles();
        dto.totalSize = op.getTotalSize();
        dto.status = op.getStatus();
        dto.errorMessage = op.getErrorMessage();
        dto.fileNames = op.getFileNames();
        dto.createdAt = op.getCreatedAt();
        dto.completedAt = op.getCompletedAt();
        dto.processingTimeMs = op.getProcessingTimeMs();
        return dto;
    }

    public String getId() {
        return id;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
}
