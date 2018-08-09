package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;

@Component
public class FileMapper {
    public static final String PATH_SEPARATOR = "/";

    public FileDto map(File file, String basePath, String requestPath) {
        FileDto fileDto = new FileDto();
        fileDto.setName(file.getName());
        fileDto.setPath(getFolderPath(basePath, requestPath) +  file.getName());
        fileDto.setSize(file.length());
        fileDto.setType(getFileType(file));

        return fileDto;
    }

    public List<FileDto> map(List<File> files, String basePath, String requestPath) {
        return files.stream().map(file -> map(file, basePath, requestPath)).collect(Collectors.toList());
    }

    public FileDto getCurrentFolderDto(String basePath, String requestPath, String fullPath) {
        File currentFolder = new File(Paths.get(fullPath).toUri());
        FileDto currentFolderDto = new FileDto();
        currentFolderDto.setType(FileType.DIR);
        currentFolderDto.setPath(getFolderPath(basePath, requestPath));
        currentFolderDto.setName(StringUtils.isEmpty(requestPath) ? basePath : currentFolder.getName());

        return currentFolderDto;
    }

    private String getFolderPath(String basePath, String paramPath) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(PATH_SEPARATOR).append(basePath);

        if (StringUtils.isNotEmpty(paramPath)) {
            pathBuilder.append(PATH_SEPARATOR).append(paramPath);
        }

        pathBuilder.append(PATH_SEPARATOR);

        return pathBuilder.toString();
    }

    private FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIR;
        }

        if (file.isFile()) {
            return FileType.FILE;
        }

        return FileType.ARCHIVE;
    }
}
