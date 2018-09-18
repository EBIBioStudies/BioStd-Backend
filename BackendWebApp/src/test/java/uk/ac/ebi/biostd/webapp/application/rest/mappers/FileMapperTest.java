package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.types.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil;

@RunWith(MockitoJUnitRunner.class)
public class FileMapperTest {
    private static final String SLASH = "/";
    private static final long FILE_SIZE = 1234L;
    private static final long FOLDER_SIZE = 5678L;
    private static final String BASE_PATH = "User";
    private static final String FOLDER_NAME = "folder";
    private static final String FILE_NAME = "file1.txt";
    private static final String FILE_PATH = "/folder/file1.txt";
    private static final String ARCHIVE_NAME = "archive1.zip";

    @Mock
    private File mockFile;

    @Mock
    private File mockDirectory;

    @Mock
    private File mockArchive;

    @Mock
    private FileUtil mockFileUtil;

    private List<File> files;
    private List<File> archives;

    @InjectMocks
    private FileMapper testInstance;

    @Before
    public void setUp() {
        files = Arrays.asList(mockFile);
        archives = Arrays.asList(mockArchive);

        when(mockFile.length()).thenReturn(FILE_SIZE);
        when(mockFile.getName()).thenReturn(FILE_NAME);

        when(mockDirectory.length()).thenReturn(FOLDER_SIZE);
        when(mockDirectory.getName()).thenReturn(FOLDER_NAME);

        when(mockArchive.length()).thenReturn(FOLDER_SIZE);
        when(mockArchive.getName()).thenReturn(ARCHIVE_NAME);

        when(mockFileUtil.getFileType(mockFile)).thenReturn(FileType.FILE);
        when(mockFileUtil.getFileType(mockDirectory)).thenReturn(FileType.DIR);
        when(mockFileUtil.getFileType(mockArchive)).thenReturn(FileType.ARCHIVE);
        when(mockFileUtil.getArchiveInnerFiles(eq(mockArchive), anyString())).thenReturn(files);
    }

    @Test
    public void mapFile() {
        FileDto fileDto = testInstance.mapFilesShowingArchive(files, BASE_PATH, FOLDER_NAME, "").get(0);
        assertFileDto(
                fileDto,
                FILE_NAME,
                FILE_SIZE,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + FILE_NAME);
    }

    @Test
    public void mapFileWithPath() {
        FileDto fileDto = testInstance.mapFile(mockFile, BASE_PATH, FOLDER_NAME + SLASH + FILE_NAME);
        assertFileDto(
                fileDto,
                FILE_NAME,
                FILE_SIZE,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + FILE_NAME);
    }

    @Test
    public void mapFilesNotShowingArchive() {
        FileDto fileDto = testInstance.mapFile(mockArchive, BASE_PATH, "");
        assertFileDto(fileDto, ARCHIVE_NAME, FOLDER_SIZE, FileType.ARCHIVE, SLASH + BASE_PATH + SLASH + ARCHIVE_NAME);
    }

    @Test
    public void mapFilesShowingArchive() {
        FileDto fileDto = testInstance.mapFilesShowingArchive(archives, BASE_PATH, ARCHIVE_NAME, FOLDER_NAME).get(0);

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
        FileDto fileDto =
                testInstance.mapFilesShowingArchive(archives, BASE_PATH, FOLDER_NAME + ARCHIVE_NAME, FILE_NAME).get(0);

        assertFileDto(
                fileDto,
                ARCHIVE_NAME,
                FileType.ARCHIVE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + ARCHIVE_NAME);
        assertThat(fileDto.getFiles()).hasSize(1);
        assertFileDto(
                fileDto.getFiles().get(0),
                FILE_NAME,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + ARCHIVE_NAME + SLASH + FILE_NAME);
    }

    @Test
    public void mapArchiveInnerSingleFileNoZipPath() {
        FileDto fileDto =
                testInstance.mapFilesShowingArchive(archives, BASE_PATH, FOLDER_NAME + SLASH + ARCHIVE_NAME, "").get(0);

        assertFileDto(
                fileDto,
                ARCHIVE_NAME,
                FileType.ARCHIVE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + ARCHIVE_NAME);
        assertThat(fileDto.getFiles()).hasSize(1);
        assertFileDto(
                fileDto.getFiles().get(0),
                FILE_NAME,
                FileType.FILE,
                SLASH + BASE_PATH + SLASH + FOLDER_NAME + SLASH + ARCHIVE_NAME + SLASH + FILE_NAME);
    }

    @Test
    public void mapList() {
        List<FileDto> files = testInstance.mapFiles(Arrays.asList(mockFile, mockDirectory), BASE_PATH, "");
        assertThat(files).hasSize(1);

        FileDto fileDto1 = files.get(0);
        FileDto fileDto2 = files.get(1);
        assertFileDto(fileDto1, FILE_NAME, FILE_SIZE, FileType.FILE, SLASH + BASE_PATH + SLASH + FILE_NAME);
        assertFileDto(fileDto2, FOLDER_NAME, FOLDER_SIZE, FileType.DIR, SLASH + BASE_PATH + SLASH + FOLDER_NAME);
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
