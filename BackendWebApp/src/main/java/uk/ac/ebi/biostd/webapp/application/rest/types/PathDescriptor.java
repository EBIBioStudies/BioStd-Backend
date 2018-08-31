package uk.ac.ebi.biostd.webapp.application.rest.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;

@Data
@AllArgsConstructor
public class PathDescriptor {
    public static final String PATH_REQUIRED_ERROR_MSG = "A file path must be specified for this operation";

    private final String path;
    private final String archivePath;

    public String getRequiredPath() {
        if (StringUtils.isEmpty(path)) {
            throw new ApiErrorException(PATH_REQUIRED_ERROR_MSG, HttpStatus.BAD_REQUEST);
        }

        return path;
    }
}
