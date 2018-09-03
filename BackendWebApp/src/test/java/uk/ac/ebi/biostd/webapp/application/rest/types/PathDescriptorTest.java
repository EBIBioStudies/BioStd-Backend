package uk.ac.ebi.biostd.webapp.application.rest.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.biostd.webapp.application.rest.types.PathDescriptor.PATH_REQUIRED_ERROR_MSG;

import org.junit.Test;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;

public class PathDescriptorTest {
    private static final String PATH = "/folder/file.txt";
    private static final String ARCHIVE_PATH = "archiveFolder/file.txt";

    @Test
    public void simplePathDescriptor() {
        PathDescriptor testInstance = new PathDescriptor(PATH, ARCHIVE_PATH);
        assertThat(testInstance.getPath()).isEqualTo(PATH);
        assertThat(testInstance.getArchivePath()).isEqualTo(ARCHIVE_PATH);
    }

    @Test
    public void requiredPathDescriptor() {
        PathDescriptor testInstance = new PathDescriptor("", "");
        assertThatExceptionOfType(ApiErrorException.class)
                .isThrownBy(() -> testInstance.getRequiredPath()).withMessage(PATH_REQUIRED_ERROR_MSG);
    }
}
