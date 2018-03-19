package uk.ac.ebi.biostd.backend.testing;

import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.biostd.util.FileUtil;

public class IntegrationTestUtil {

    public static String initFileSystem(TemporaryFolder TEST_FOLDER) throws IOException {
        String NFS_PATH = TEST_FOLDER.getRoot().getPath();
        System.setProperty(BIOSTUDY_BASE_DIR, NFS_PATH);

        File miscDir = new File(NFS_PATH + "/misc");
        miscDir.mkdir();

        FileUtil.copyDirectory(new ClassPathResource("nfs/misc").getFile(), miscDir);
        writeConfigFile(new ClassPathResource("nfs/config.properties").getFile(), NFS_PATH);

        return NFS_PATH;
    }

    private static void writeConfigFile(File file, String nfsPath) throws IOException {
        String content = FileUtils.readFileToString(file).replaceAll("\\{BASE_PATH\\}", nfsPath);
        FileUtils.writeStringToFile(new File(nfsPath + "/config.properties"), content);
    }
}
