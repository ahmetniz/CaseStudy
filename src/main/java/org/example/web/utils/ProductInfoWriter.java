package org.example.web.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductInfoWriter {

    private static final Logger LOG = LogManager.getLogger(ProductInfoWriter.class);
    private static final Path OUTPUT_DIR = Path.of("test-output");

    private final Path file;

    public ProductInfoWriter(String fileName) {
        this.file = OUTPUT_DIR.resolve(fileName);
    }

    public void write(String productName, String price) {
        try {
            Files.createDirectories(OUTPUT_DIR);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String line = String.format("[%s] Product: %s | Price: %s%n", timestamp, productName, price);
            Files.writeString(file, line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            LOG.info("Wrote product info to {}: name='{}', price='{}'", file.toAbsolutePath(), productName, price);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write product info to " + file, e);
        }
    }
}
