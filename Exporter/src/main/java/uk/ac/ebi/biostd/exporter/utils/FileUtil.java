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

    public static String filesAsStringList(List<File> files) {
        StringBuilder builder = new StringBuilder();
        files.forEach(file -> {
            builder.append("- ");
            builder.append(file.toPath());
            builder.append("\n");
        });

        return builder.toString();
    }

    public static File[] listFilesMatching(String rootPath, String regex) {
        Pattern p = Pattern.compile(regex);
        return new File(rootPath).listFiles(file -> p.matcher(file.getName()).matches());
    }
}
