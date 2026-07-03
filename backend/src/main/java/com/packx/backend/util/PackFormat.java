package com.packx.backend.util;

import com.packx.backend.exception.FileProcessingException;
import com.packx.backend.exception.InvalidPackedFileException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Everything about the on-disk ".pack" format lives here, in one place,
 * instead of being duplicated across the packing and unpacking services.
 *
 * Format (unchanged from the original console program, byte-for-byte):
 *
 *   [100-byte header][file bytes] [100-byte header][file bytes] ...
 *
 * Header = "<filename> <filesize>", space-padded to exactly 100 bytes.
 *
 * Original bug fixed here: the console app parsed the header with
 * `Header.split(" ")`, which breaks for filenames containing spaces
 * because Tokens[0] would only be the first word of the name. This class
 * instead splits on the LAST space in the trimmed header, so "my file.txt 120"
 * is parsed as filename="my file.txt", size=120. This is a pure parsing fix -
 * the bytes on disk are identical, so packed files produced by the original
 * Java program (with space-free filenames) remain fully readable, and files
 * packed by PackX remain unpackable by the original program as long as the
 * filename itself has no spaces.
 */
public final class PackFormat {

    public static final int HEADER_SIZE = 100;
    public static final byte XOR_KEY = 0x11;
    public static final int BUFFER_SIZE = 8192;

    private PackFormat() {
    }

    public static byte[] buildHeader(String fileName, long fileSize) {
        String header = fileName + " " + fileSize;
        if (header.length() > HEADER_SIZE) {
            throw new InvalidPackedFileException(
                    "Filename is too long to fit in a " + HEADER_SIZE + "-byte header: " + fileName);
        }
        StringBuilder padded = new StringBuilder(header);
        while (padded.length() < HEADER_SIZE) {
            padded.append(' ');
        }
        return padded.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static ParsedHeader parseHeader(byte[] headerBytes) {
        String header = new String(headerBytes, StandardCharsets.UTF_8).trim();
        if (header.isEmpty()) {
            throw new InvalidPackedFileException("Corrupted packed file: empty header block");
        }
        int lastSpace = header.lastIndexOf(' ');
        if (lastSpace <= 0 || lastSpace == header.length() - 1) {
            throw new InvalidPackedFileException("Corrupted packed file: malformed header \"" + header + "\"");
        }
        String fileName = header.substring(0, lastSpace);
        String sizeToken = header.substring(lastSpace + 1);
        long size;
        try {
            size = Long.parseLong(sizeToken);
        } catch (NumberFormatException e) {
            throw new InvalidPackedFileException("Corrupted packed file: invalid file size in header");
        }
        if (size < 0) {
            throw new InvalidPackedFileException("Corrupted packed file: negative file size");
        }
        return new ParsedHeader(fileName, size);
    }

    /**
     * Reads exactly {@code length} bytes from the stream, looping as needed.
     * Fixes the original bug where a single call to InputStream#read was
     * trusted to return the full requested length, which the Java docs make
     * no such guarantee about.
     */
    public static byte[] readFully(InputStream in, long length) {
        if (length > Integer.MAX_VALUE - 8) {
            throw new FileProcessingException("File is too large to process: " + length + " bytes");
        }
        byte[] result = new byte[(int) length];
        int totalRead = 0;
        try {
            while (totalRead < result.length) {
                int read = in.read(result, totalRead, result.length - totalRead);
                if (read == -1) {
                    throw new InvalidPackedFileException(
                            "Corrupted packed file: expected " + length + " bytes but stream ended after " + totalRead);
                }
                totalRead += read;
            }
        } catch (IOException e) {
            throw new FileProcessingException("I/O error while reading packed data", e);
        }
        return result;
    }

    public static void xorInPlace(byte[] buffer, int length) {
        for (int i = 0; i < length; i++) {
            buffer[i] = (byte) (buffer[i] ^ XOR_KEY);
        }
    }

    public record ParsedHeader(String fileName, long fileSize) {
    }
}
