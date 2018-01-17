package uk.ac.ebi.biostd.exporter.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {

    @SneakyThrows
    public String readFile(String filePath) {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
