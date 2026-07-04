package com.packx.backend.controller;

import com.packx.backend.dto.OperationResponse;
import com.packx.backend.model.Operation;
import com.packx.backend.service.PackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class PackController {

    private final PackingService packingService;

    public PackController(PackingService packingService) {
        this.packingService = packingService;
    }

    @PostMapping(value = "/api/pack", consumes = "multipart/form-data")
    public ResponseEntity<OperationResponse> pack(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("packName") String packName) {
        Operation operation = packingService.pack(files, packName);
        return ResponseEntity.ok(OperationResponse.from(operation));
    }
}
