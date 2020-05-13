package uk.ac.ebi.biostd.exporter.utils;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {

    @SneakyThrows
    public String readFile(String filePath) {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    @SneakyThrows
    public List<File> listFiles(String filePath) {
        return Files.list(Paths.get(filePath)).map(Path::toFile).collect(toList());
    }

    @SneakyThrows
    public static List<File> listFilesMatching(String rootPath, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return Files.list(Paths.get(rootPath))
            .map(Path::toFile)
            .filter(file -> pattern.matcher(file.getName()).matches())
            .collect(toList());
    }
}
