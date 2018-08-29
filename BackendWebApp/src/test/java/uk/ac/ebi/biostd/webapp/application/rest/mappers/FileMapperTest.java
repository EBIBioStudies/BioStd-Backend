package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
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
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;

@RunWith(MockitoJUnitRunner.class)
public class FileMapperTest {
    private static final String SLASH = "/";
    private final static long FILE_SIZE = 1234L;
    private final static long FOLDER_SIZE = 5678L;
    private final static String BASE_PATH = "User";
    private final static String FOLDER_NAME = "folder";
    private final static String FILE_NAME = "file1.txt";
    private final static String FILE_CONTENT = "test content";
    private final static String FILE_PATH = "/folder/file1.txt";
    private final static String ARCHIVE_NAME = "archive1.zip";
    private final static String SINGLE_FILE_ARCHIVE = "archive2.zip";

    @Rule
    public TemporaryFolder mockFileSystem = new TemporaryFolder();

    @Mock
    private File mockFile1;

    @Mock
    private File mockFile2;

    @Mock
    private File mockFile3;

    private File zipFile;

    private File singleFileZip;

    private FileMapper testInstance;

    @Before
    public void setUp() throws Exception {
        testInstance = new FileMapper();
        mockFileSystem.newFolder(FOLDER_NAME);

        zipFile = mockFileSystem.newFile(ARCHIVE_NAME);
        createTestZipFile(zipFile, FILE_PATH);

        singleFileZip = mockFileSystem.newFile(SINGLE_FILE_ARCHIVE);
        createTestZipFile(singleFileZip, FILE_NAME);

        when(mockFile1.length()).thenReturn(FILE_SIZE);
        when(mockFile1.getName()).thenReturn(FILE_NAME);

        when(mockFile2.isDirectory()).thenReturn(true);
        when(mockFile2.length()).thenReturn(FOLDER_SIZE);
        when(mockFile2.getName()).thenReturn(FOLDER_NAME);

        when(mockFile3.isDirectory()).thenReturn(false);
        when(mockFile3.length()).thenReturn(FOLDER_SIZE);
        when(mockFile3.getName()).thenReturn(ARCHIVE_NAME);
    }

    @Test
    public void map() {
        FileDto fileDto = testInstance.map(mockFile1, BASE_PATH, FOLDER_NAME, true, "");
        assertFileDto(
                fileDto,
                FILE_NAME,
                FILE_SIZE,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + FILE_NAME);
    }

    @Test
    public void mapWithPath() {
        FileDto fileDto = testInstance.map(mockFile1, BASE_PATH + SLASH + FOLDER_NAME + SLASH + FILE_NAME);
        assertFileDto(
                fileDto,
                FILE_NAME,
                FILE_SIZE,
                FileType.FILE,
                BASE_PATH + SLASH + FOLDER_NAME + SLASH + FILE_NAME);
    }

    @Test
    public void mapArchive() {
        FileDto fileDto = testInstance.map(mockFile3, BASE_PATH, "");
        assertFileDto(fileDto, ARCHIVE_NAME, FOLDER_SIZE, FileType.ARCHIVE, SLASH + BASE_PATH + SLASH + ARCHIVE_NAME);
    }

    @Test
    public void mapArchiveInnerFiles() {
        FileDto fileDto = testInstance.map(zipFile, BASE_PATH, ARCHIVE_NAME, true, FOLDER_NAME);

        assertFileDto(fileDto, ARCHIVE_NAME, FileType.ARCHIVE, SLASH + BASE_PATH + SLASH + ARCHIVE_NAME);
        assertThat(fileDto.getFiles()).hasSize(1);
        assertFileDto(
                fileDto.getFiles().get(0),
                FILE_NAME,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + ARCHIVE_NAME + FILE_PATH);
    }

    @Test
    public void mapArchiveInnerSingleFile() {
        FileDto fileDto = testInstance.map(singleFileZip, BASE_PATH, FOLDER_NAME + SINGLE_FILE_ARCHIVE, true, FILE_NAME);

        assertFileDto(
                fileDto,
                SINGLE_FILE_ARCHIVE,
                FileType.ARCHIVE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SINGLE_FILE_ARCHIVE);
        assertThat(fileDto.getFiles()).hasSize(1);
        assertFileDto(
                fileDto.getFiles().get(0),
                FILE_NAME,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + SINGLE_FILE_ARCHIVE + SLASH + FILE_NAME);
    }

    @Test
    public void mapArchiveInnerSingleFileNoZipPath() {
        FileDto fileDto = testInstance.map(singleFileZip, BASE_PATH, FOLDER_NAME + SLASH + SINGLE_FILE_ARCHIVE, true, "");

        assertFileDto(
                fileDto,
                SINGLE_FILE_ARCHIVE,
                FileType.ARCHIVE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + SINGLE_FILE_ARCHIVE);
        assertThat(fileDto.getFiles()).hasSize(1);
        assertFileDto(
                fileDto.getFiles().get(0),
                FILE_NAME,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + SINGLE_FILE_ARCHIVE + SLASH + FILE_NAME);
    }

    @Test
    public void mapArchiveInnerSingleFileNoShowArchive() {
        FileDto fileDto = testInstance.map(singleFileZip, BASE_PATH, FOLDER_NAME + SINGLE_FILE_ARCHIVE, false, FILE_NAME);

        assertFileDto(
                fileDto,
                SINGLE_FILE_ARCHIVE,
                FileType.ARCHIVE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SINGLE_FILE_ARCHIVE);
        assertThat(fileDto.getFiles()).isNull();
    }

    @Test
    public void mapBrokenArchive() {
        File brokenArchive = mock(File.class);
        when(brokenArchive.getName()).thenReturn(ARCHIVE_NAME);

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> testInstance.map(brokenArchive, BASE_PATH, FOLDER_NAME + SINGLE_FILE_ARCHIVE, true, FILE_NAME));
    }

    @Test
    public void mapList() {
        List<FileDto> files = testInstance.map(Arrays.asList(mockFile1, mockFile2), BASE_PATH, "");
        assertThat(files).hasSize(2);

        FileDto fileDto1 = files.get(0);
        FileDto fileDto2 = files.get(1);
        assertFileDto(fileDto1, FILE_NAME, FILE_SIZE, FileType.FILE, SLASH + BASE_PATH + SLASH + FILE_NAME);
        assertFileDto(fileDto2, FOLDER_NAME, FOLDER_SIZE, FileType.DIR, SLASH + BASE_PATH + SLASH + FOLDER_NAME);
    }

    private void createTestZipFile(File zipFile, String path) throws Exception {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOutputStream.putNextEntry(new ZipEntry(path));
        zipOutputStream.write(FILE_CONTENT.getBytes());
        zipOutputStream.close();
    }

    private void assertFileDto(FileDto fileDto, String name, FileType fileType, String path) {
        assertThat(fileDto.getName()).isEqualTo(name);
        assertThat(fileDto.getType()).isEqualTo(fileType);
        assertThat(fileDto.getPath()).isEqualTo(path);
    }

    private void assertFileDto(FileDto fileDto, String name, long size, FileType fileType, String path) {
        assertThat(fileDto.getSize()).isEqualTo(size);
        assertFileDto(fileDto, name, fileType, path);
    }
}
