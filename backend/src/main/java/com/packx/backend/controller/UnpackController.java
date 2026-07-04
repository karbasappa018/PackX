package com.packx.backend.controller;

import com.packx.backend.dto.OperationResponse;
import com.packx.backend.service.UnpackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UnpackController {

    private final UnpackingService unpackingService;

    public UnpackController(UnpackingService unpackingService) {
        this.unpackingService = unpackingService;
    }

    @PostMapping(value = "/api/unpack", consumes = "multipart/form-data")
    public ResponseEntity<OperationResponse> unpack(@RequestParam("file") MultipartFile file) {
        UnpackingService.UnpackResult result = unpackingService.unpack(file);
        return ResponseEntity.ok(OperationResponse.from(result.operation()));
    }
}
