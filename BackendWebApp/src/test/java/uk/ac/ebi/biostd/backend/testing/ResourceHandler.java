package uk.ac.ebi.biostd.backend.testing;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

@UtilityClass
public class ResourceHandler {

    @SneakyThrows
    public static File getResourceFile(String relativeResourcePath) {
        return new ClassPathResource(relativeResourcePath).getFile();
    }

    @SneakyThrows
    public static String getResourceFileAsString(String relativeResourcePath) {
        File file = new ClassPathResource(relativeResourcePath).getFile();
        return getFileAsString(file);
    }

    public static String readFile(String filePath) {
        File file = new File(filePath);
        return getFileAsString(file);
    }

    @SneakyThrows
    private static String getFileAsString(File file) {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, UTF_8);
    }
}
