package com.packx.backend.controller;

import com.packx.backend.dto.DashboardStatsResponse;
import com.packx.backend.service.OperationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final OperationService operationService;

    public DashboardController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/api/dashboard/stats")
    public DashboardStatsResponse stats() {
        return operationService.computeStats();
    }
}
