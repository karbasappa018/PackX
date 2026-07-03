package com.packx.backend.service;

import com.packx.backend.dto.DashboardStatsResponse;
import com.packx.backend.exception.OperationNotFoundException;
import com.packx.backend.model.Operation;
import com.packx.backend.model.OperationStatus;
import com.packx.backend.model.OperationType;
import com.packx.backend.repository.OperationRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;


@Service
public class OperationService {

    private final OperationRepository repository;

    public OperationService(OperationRepository repository) {
        this.repository = repository;
    }

    public Operation startOperation(OperationType type, String inputFileName, String outputFileName) {
        Operation operation = new Operation();
        operation.setOperationType(type);
        operation.setInputFileName(inputFileName);
        operation.setOutputFileName(outputFileName);
        operation.setStatus(OperationStatus.PENDING);
        operation.setCreatedAt(Instant.now());
        return repository.save(operation);
    }

    public Operation completeOperation(Operation operation, Path resultPath, int numberOfFiles,
                                        long totalSize, long processingTimeMs) {
        operation.setStatus(OperationStatus.SUCCESS);
        operation.setStoredFilePath(resultPath.toAbsolutePath().toString());
        operation.setOutputFileName(resultPath.getFileName().toString());
        operation.setNumberOfFiles(numberOfFiles);
        operation.setTotalSize(totalSize);
        operation.setCompletedAt(Instant.now());
        operation.setProcessingTimeMs(processingTimeMs);
        return repository.save(operation);
    }

    public Operation failOperation(Operation operation, String errorMessage) {
        operation.setStatus(OperationStatus.FAILED);
        operation.setErrorMessage(errorMessage);
        operation.setCompletedAt(Instant.now());
        return repository.save(operation);
    }

    public Operation save(Operation operation) {
        return repository.save(operation);
    }

    public List<Operation> listAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Operation getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new OperationNotFoundException("No operation found with id " + id));
    }

    public void delete(String id) {
        Operation operation = getById(id);
        repository.delete(operation);
    }

    public DashboardStatsResponse computeStats() {
        List<Operation> all = repository.findAll();

        long totalOperations = all.size();
        long filesPacked = all.stream()
                .filter(op -> op.getOperationType() == OperationType.PACK && op.getStatus() == OperationStatus.SUCCESS)
                .mapToLong(Operation::getNumberOfFiles)
                .sum();
        long filesUnpacked = all.stream()
                .filter(op -> op.getOperationType() == OperationType.UNPACK && op.getStatus() == OperationStatus.SUCCESS)
                .mapToLong(Operation::getNumberOfFiles)
                .sum();
        long totalDataProcessed = all.stream()
                .filter(op -> op.getStatus() == OperationStatus.SUCCESS)
                .mapToLong(Operation::getTotalSize)
                .sum();
        long totalPacks = all.stream().filter(op -> op.getOperationType() == OperationType.PACK).count();
        long totalUnpacks = all.stream().filter(op -> op.getOperationType() == OperationType.UNPACK).count();

        List<Operation> recent = repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(5)
                .toList();

        return new DashboardStatsResponse(
                totalOperations, totalPacks, totalUnpacks, filesPacked, filesUnpacked, totalDataProcessed, recent);
    }
}
