package uk.ac.ebi.biostd.webapp.application.rest.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.EntityNotFoundException;
import uk.ac.ebi.biostd.webapp.application.rest.types.FileType;

@UtilityClass
public class FileUtil {
    public static final String PATH_SEPARATOR = "/";
    public static final String TMP_DIR = "temp-extract";
    public static final String ARCHIVE_EXTENSION = ".zip";
    public static final String ARCHIVE_INNER_FILE_NOT_FOUND_ERROR_MSG = "File not found inside zip: %s/%s";

    @SneakyThrows
    public static List<File> getArchiveInnerFiles(File archive, String archiveInnerPath) {
        File tmpDir = new File(FileUtils.getTempDirectory().getAbsolutePath() + PATH_SEPARATOR + TMP_DIR);
        ZipUtil.unpack(archive, tmpDir);
        Path filesPath = tmpDir.toPath().resolve(archiveInnerPath);

        if (Files.notExists(filesPath)) {
            throw new EntityNotFoundException(
                    String.format(ARCHIVE_INNER_FILE_NOT_FOUND_ERROR_MSG, archive.getName(), archiveInnerPath),
                    archive.getClass());
        }

        List<File> zipFiles =
                Files.isDirectory(filesPath) ?
                Files.list(filesPath).map(entry -> new File(entry.toUri())).collect(Collectors.toList()) :
                Arrays.asList(new File(filesPath.toUri()));
        FileUtils.deleteDirectory(tmpDir);

        return zipFiles;
    }

    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIR;
        }

        if (file.getName().endsWith(ARCHIVE_EXTENSION)) {
            return FileType.ARCHIVE;
        }

        return FileType.FILE;
    }
}
