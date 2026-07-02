package com.packx.backend.dto;

import com.packx.backend.model.Operation;

import java.util.List;

public class DashboardStatsResponse {

    private final long totalOperations;
    private final long totalPacks;
    private final long totalUnpacks;
    private final long filesPacked;
    private final long filesUnpacked;
    private final long totalDataProcessedBytes;
    private final List<OperationResponse> recentOperations;

    public DashboardStatsResponse(long totalOperations, long totalPacks, long totalUnpacks,
                                   long filesPacked, long filesUnpacked, long totalDataProcessedBytes,
                                   List<Operation> recentOperations) {
        this.totalOperations = totalOperations;
        this.totalPacks = totalPacks;
        this.totalUnpacks = totalUnpacks;
        this.filesPacked = filesPacked;
        this.filesUnpacked = filesUnpacked;
        this.totalDataProcessedBytes = totalDataProcessedBytes;
        this.recentOperations = recentOperations.stream().map(OperationResponse::from).toList();
    }

    public long getTotalOperations() {
        return totalOperations;
    }

    public long getTotalPacks() {
        return totalPacks;
    }

    public long getTotalUnpacks() {
        return totalUnpacks;
    }

    public long getFilesPacked() {
        return filesPacked;
    }

    public long getFilesUnpacked() {
        return filesUnpacked;
    }

    public long getTotalDataProcessedBytes() {
        return totalDataProcessedBytes;
    }

    public List<OperationResponse> getRecentOperations() {
        return recentOperations;
    }
}
