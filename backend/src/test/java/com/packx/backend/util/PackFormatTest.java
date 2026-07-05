package com.packx.backend.util;

import com.packx.backend.exception.InvalidPackedFileException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class PackFormatTest {

    @Test
    void buildAndParseHeaderRoundTrips() {
        byte[] header = PackFormat.buildHeader("document1.txt", 12345L);
        assertEquals(PackFormat.HEADER_SIZE, header.length);

        PackFormat.ParsedHeader parsed = PackFormat.parseHeader(header);
        assertEquals("document1.txt", parsed.fileName());
        assertEquals(12345L, parsed.fileSize());
    }

    @Test
    void parseHeaderHandlesFilenamesWithSpaces() {
        // This is the fix for the original Header.split(" ") bug.
        byte[] header = PackFormat.buildHeader("my important notes.txt", 42L);
        PackFormat.ParsedHeader parsed = PackFormat.parseHeader(header);

        assertEquals("my important notes.txt", parsed.fileName());
        assertEquals(42L, parsed.fileSize());
    }

    @Test
    void parseHeaderRejectsCorruptHeader() {
        byte[] garbage = new byte[PackFormat.HEADER_SIZE];
        java.util.Arrays.fill(garbage, (byte) ' ');
        assertThrows(InvalidPackedFileException.class, () -> PackFormat.parseHeader(garbage));
    }

    @Test
    void filenameTooLongThrows() {
        String longName = "a".repeat(100) + ".txt";
        assertThrows(InvalidPackedFileException.class, () -> PackFormat.buildHeader(longName, 10));
    }

    @Test
    void readFullyReadsExactLength() {
        byte[] data = "hello world".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] result = PackFormat.readFully(in, data.length);
        assertArrayEquals(data, result);
    }

    @Test
    void readFullyThrowsWhenStreamEndsEarly() {
        byte[] data = "short".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        assertThrows(InvalidPackedFileException.class, () -> PackFormat.readFully(in, 100));
    }
}
