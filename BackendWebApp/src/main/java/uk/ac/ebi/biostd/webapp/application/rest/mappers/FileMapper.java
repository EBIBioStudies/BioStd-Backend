package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import static uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil.PATH_SEPARATOR;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.types.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil;

@Component
@AllArgsConstructor
public class FileMapper {
    private final FileUtil fileUtil;

    public FileDto mapFile(File file, String pathPrefix, String path) {
        return map(file, pathPrefix, path, false, "");
    }

    public List<FileDto> mapFiles(List<File> files, String pathPrefix, String path) {
        return mapList(files, pathPrefix, path, false, "");
    }

    public List<FileDto> mapFilesShowingArchive(
            List<File> files, String pathPrefix, String path, String archivePath) {
        return mapList(files, pathPrefix, path, true, archivePath);
    }

    private FileDto map(File file, String pathPrefix, String path, boolean showArchive, String archivePath) {
        String fullPath = getFolderPath(pathPrefix, path);
        fullPath = fullPath.contains(file.getName()) ? fullPath : fullPath + PATH_SEPARATOR + file.getName();

        FileDto mappedFile = mapToDto(file, fullPath);
        if (showArchive && mappedFile.getType() == FileType.ARCHIVE) {
            List<File> archiveInnerFiles = fileUtil.getArchiveInnerFiles(file, archivePath);
            String archiveFolderPath = getArchiveFolderPath(path, file.getName(), archivePath);
            mappedFile.setFiles(mapList(archiveInnerFiles, pathPrefix, archiveFolderPath, false, ""));
        }

        return mappedFile;
    }

    private List<FileDto> mapList(
            List<File> files, String pathPrefix, String path, boolean showArchive, String archivePath) {
        return files.stream().map(
                file -> map(file, pathPrefix, path, showArchive, archivePath)).collect(Collectors.toList());
    }

    private FileDto mapToDto(File file, String path) {
        FileDto fileDto = new FileDto();
        fileDto.setName(file.getName());
        fileDto.setPath(path);
        fileDto.setSize(file.length());
        fileDto.setType(fileUtil.getFileType(file));

        return fileDto;
    }

    private String getArchiveFolderPath(String path, String archiveName, String archivePath) {
        StringBuilder zipPathBuilder = new StringBuilder();
        String noZipRequestPath = StringUtils.substringBefore(path, archiveName);

        if (StringUtils.isNotEmpty(noZipRequestPath)) {
            zipPathBuilder.append(noZipRequestPath);

            if(!noZipRequestPath.endsWith(PATH_SEPARATOR)) {
                zipPathBuilder.append(PATH_SEPARATOR);
            }
        }

        zipPathBuilder.append(archiveName);

        if (StringUtils.isNotEmpty(archivePath)) {
            zipPathBuilder.append(PATH_SEPARATOR).append(archivePath);
        }

        return zipPathBuilder.toString();
    }

    private String getFolderPath(String pathPrefix, String path) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(PATH_SEPARATOR).append(pathPrefix);

        if (StringUtils.isNotEmpty(path)) {
            pathBuilder.append(PATH_SEPARATOR).append(path);
        }

        return pathBuilder.toString();
    }
}
