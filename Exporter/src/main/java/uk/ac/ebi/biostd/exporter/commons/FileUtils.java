package uk.ac.ebi.biostd.exporter.commons;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * Wrappers class that encapsulate file operations to facilitate unit testing.
 */
@Component
public class FileUtils {

    @SneakyThrows
    public Path copy(Path source, Path target, CopyOption... option) {
        return Files.copy(source, target, option);
    }

    @SneakyThrows
    public void delete(Path path) {
        Files.delete(path);
    }
}
