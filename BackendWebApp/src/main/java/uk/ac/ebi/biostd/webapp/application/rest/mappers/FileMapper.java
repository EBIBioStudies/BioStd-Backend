package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;

@Component
public class FileMapper {
    public static final String ARCHIVE_EXTENSION = ".zip";
    public static final String PATH_SEPARATOR = "/";
    public static final String TMP_DIR = "temp-extract";

    public FileDto map(File file, String path) {
        FileDto fileDto = new FileDto();
        fileDto.setName(file.getName());
        fileDto.setPath(path);
        fileDto.setSize(file.length());
        fileDto.setType(getFileType(file));

        return fileDto;
    }

    public FileDto map(File file, String basePath, String requestPath) {
        return map(file, basePath, requestPath, false, "");
    }

    public FileDto map(File file, String basePath, String requestPath, boolean showArchive, String zipPath) {
        String path = getFolderPath(basePath, requestPath);
        path = path.contains(file.getName()) ? path : path + PATH_SEPARATOR + file.getName();

        FileDto mappedFile = map(file, path);
        if (showArchive && mappedFile.getType() == FileType.ARCHIVE) {
            mappedFile.setFiles(mapZipInnerFiles(file, basePath, requestPath, zipPath));
        }

        return mappedFile;
    }

    public List<FileDto> map(List<File> files, String basePath, String requestPath) {
        return map(files, basePath, requestPath, false, "");
    }

    public List<FileDto> map(List<File> files, String basePath, String requestPath, boolean showArchive, String zipPath) {
        return files.stream().map(
                file -> map(file, basePath, requestPath, showArchive, zipPath)).collect(Collectors.toList());
    }

    @SneakyThrows
    private List<FileDto> mapZipInnerFiles(File zipFile, String basePath, String requestPath, String zipPath) {
        File tmpDir = new File(FileUtils.getTempDirectory().getAbsolutePath() + PATH_SEPARATOR + TMP_DIR);
        ZipUtil.unpack(zipFile, tmpDir);
        Path filesPath = tmpDir.toPath().resolve(zipPath);
        List<File> zipFiles =
                Files.isDirectory(filesPath) ?
                Files.list(filesPath).map(entry -> new File(entry.toUri())).collect(Collectors.toList()) :
                Arrays.asList(new File(filesPath.toUri()));
        FileUtils.deleteDirectory(tmpDir);

        return map(zipFiles, basePath, getZipFolderPath(requestPath, zipFile.getName(), zipPath));
    }

    private String getFolderPath(String basePath, String paramPath) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(PATH_SEPARATOR).append(basePath);

        if (StringUtils.isNotEmpty(paramPath)) {
            pathBuilder.append(PATH_SEPARATOR).append(paramPath);
        }

        return pathBuilder.toString();
    }

    private String getZipFolderPath(String requestPath, String zipName, String zipPath) {
        StringBuilder zipPathBuilder = new StringBuilder();
        String noZipRequestPath = StringUtils.substringBefore(requestPath, zipName);

        if (StringUtils.isNotEmpty(noZipRequestPath)) {
            zipPathBuilder.append(noZipRequestPath);

            if(!noZipRequestPath.endsWith(PATH_SEPARATOR)) {
                zipPathBuilder.append(PATH_SEPARATOR);
            }
        }

        zipPathBuilder.append(zipName);

        if (StringUtils.isNotEmpty(zipPath)) {
            zipPathBuilder.append(PATH_SEPARATOR).append(zipPath);
        }

        return zipPathBuilder.toString();
    }

    private FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIR;
        }

        if (file.getName().endsWith(ARCHIVE_EXTENSION)) {
            return FileType.ARCHIVE;
        }

        return FileType.FILE;
    }
}
