package com.packx.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    void transformIsSymmetric() {
        byte[] original = "Hello, PackX!".getBytes();
        byte[] copy = original.clone();

        encryptionService.transform(copy, copy.length);
        assertEquals(false, arraysEqualIgnoringLength(original, copy), "Data should change after one XOR pass");

        encryptionService.transform(copy, copy.length);
        assertArrayEquals(original, copy, "XORing twice with the same key should restore the original bytes");
    }

    @Test
    void onlyTransformsRequestedLength() {
        byte[] buffer = new byte[]{1, 2, 3, 4, 5};
        byte[] beforeTail = buffer.clone();

        encryptionService.transform(buffer, 3);

        // bytes 3 and 4 (index) must be untouched
        assertEquals(beforeTail[3], buffer[3]);
        assertEquals(beforeTail[4], buffer[4]);
    }

    private boolean arraysEqualIgnoringLength(byte[] a, byte[] b) {
        return java.util.Arrays.equals(a, b);
    }
}
