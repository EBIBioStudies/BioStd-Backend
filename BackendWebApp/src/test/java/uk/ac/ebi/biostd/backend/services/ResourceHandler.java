package uk.ac.ebi.biostd.backend.services;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

public class ResourceHandler {

    @SneakyThrows
    public File getResourceFile(String relativeResourcePath) {
        return new ClassPathResource(relativeResourcePath).getFile();
    }

    @SneakyThrows
    public String readResource(String relativeResourcePath) {
        File file = new ClassPathResource(relativeResourcePath).getFile();
        return getFileAsString(file);
    }

    @SneakyThrows
    public String readFile(String filePath) {
        File file = new File(filePath);
        return getFileAsString(file);
    }

    @SneakyThrows
    private String getFileAsString(File file) {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, UTF_8);
    }
}
