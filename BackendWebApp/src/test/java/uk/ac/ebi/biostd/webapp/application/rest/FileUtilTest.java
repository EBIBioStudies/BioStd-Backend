package uk.ac.ebi.biostd.webapp.application.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.EntityNotFoundException;
import uk.ac.ebi.biostd.webapp.application.rest.types.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil;

@RunWith(MockitoJUnitRunner.class)
public class FileUtilTest {
    private static final String FOLDER_NAME = "folder";
    private static final String INNER_FILE_NAME = "file.txt";
    private static final String ARCHIVE_NAME = "archive.zip";
    private static final String FILE_CONTENT = "some content";
    private static final String ARCHIVE_INNER_PATH = "folder/file.txt";

    @Rule
    public TemporaryFolder mockFileSystem = new TemporaryFolder();

    @Mock
    private File mockFile;

    @Mock
    private File mockFolder;

    private File archive;
    private FileUtil testInstance;

    @Before
    public void setUp() throws Exception {
        testInstance = new FileUtil();
        archive = mockFileSystem.newFile(ARCHIVE_NAME);

        setUpMockFiles();
        populateArchive();
    }

    @Test
    public void getArchiveFileType() {
        assertThat(testInstance.getFileType(mockFile)).isEqualTo(FileType.FILE);
        assertThat(testInstance.getFileType(mockFolder)).isEqualTo(FileType.DIR);
        assertThat(testInstance.getFileType(archive)).isEqualTo(FileType.ARCHIVE);
    }

    @Test
    public void getArchiveRootInnerFiles() {
        assertInnerFiles("", FOLDER_NAME);
    }

    @Test
    public void getArchiveInnerFolderFiles() {
        assertInnerFiles(ARCHIVE_INNER_PATH, INNER_FILE_NAME);
    }

    @Test
    public void getNonExistingArchiveInnerFile() {
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(
                () -> testInstance.getArchiveInnerFiles(archive, INNER_FILE_NAME));
    }

    private void assertInnerFiles(String innerPath, String expectedInnerFileName) {
        List<File> files = testInstance.getArchiveInnerFiles(archive, innerPath);
        assertThat(files).hasSize(1);

        File file = files.get(0);
        assertThat(file.getName()).isEqualTo(expectedInnerFileName);
    }

    private void populateArchive() throws Exception {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(archive));
        zipOutputStream.putNextEntry(new ZipEntry(ARCHIVE_INNER_PATH));
        zipOutputStream.write(FILE_CONTENT.getBytes());
        zipOutputStream.close();
    }

    private void setUpMockFiles() {
        when(mockFile.isDirectory()).thenReturn(false);
        when(mockFile.getName()).thenReturn(INNER_FILE_NAME);

        when(mockFolder.isDirectory()).thenReturn(true);
    }
}
