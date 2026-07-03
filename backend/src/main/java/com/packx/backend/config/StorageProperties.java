package com.packx.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "app")
public class StorageProperties {

    private final Storage storage = new Storage();
    private final Packing packing = new Packing();
    private final Cors cors = new Cors();

    public Storage getStorage() {
        return storage;
    }

    public Packing getPacking() {
        return packing;
    }

    public Cors getCors() {
        return cors;
    }

    public static class Storage {
        private String uploadDir = "storage/uploads";
        private String outputDir = "storage/output";
        private String extractedDir = "storage/extracted";

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public String getExtractedDir() {
            return extractedDir;
        }

        public void setExtractedDir(String extractedDir) {
            this.extractedDir = extractedDir;
        }
    }

    public static class Packing {
        private List<String> allowedExtensions = List.of(".txt");
        private int maxFilesPerOperation = 100;

        public List<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        public void setAllowedExtensions(List<String> allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
        }

        public int getMaxFilesPerOperation() {
            return maxFilesPerOperation;
        }

        public void setMaxFilesPerOperation(int maxFilesPerOperation) {
            this.maxFilesPerOperation = maxFilesPerOperation;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}
