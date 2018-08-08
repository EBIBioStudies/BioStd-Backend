package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;

@Component
public class FileMapper {
    public static final String PATH_SEPARATOR = "/";

    public FileDto map(String basePath, Path realPath, String requestPath) {
        File file = new File(realPath.toUri());
        FileDto fileDto = new FileDto();
        fileDto.setName(file.getName());
        fileDto.setPath(getFolderPath(basePath, requestPath) +  file.getName());
        fileDto.setSize(file.length());
        fileDto.setType(getFileType(file));

        return fileDto;
    }

    public FileDto getCurrentFolderDto(String basePath, String request, String fullPath) {
        File currentFolder = new File(Paths.get(fullPath).toUri());
        FileDto userFolderDto = new FileDto();
        userFolderDto.setType(FileType.DIR);
        userFolderDto.setPath(getFolderPath(basePath, request));
        userFolderDto.setName(StringUtils.isEmpty(request) ? basePath : currentFolder.getName());

        return userFolderDto;
    }

    private String getFolderPath(String basePath, String paramPath) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(PATH_SEPARATOR).append(basePath);

        if (!StringUtils.isEmpty(paramPath)) {
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
