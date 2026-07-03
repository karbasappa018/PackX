package com.packx.backend.service;

import com.packx.backend.util.PackFormat;
import org.springframework.stereotype.Service;


@Service
public class EncryptionService {

    public byte[] transform(byte[] buffer, int length) {
        PackFormat.xorInPlace(buffer, length);
        return buffer;
    }


    public String algorithmName() {
        return "XOR-8bit (legacy, NOT secure)";
    }
}
