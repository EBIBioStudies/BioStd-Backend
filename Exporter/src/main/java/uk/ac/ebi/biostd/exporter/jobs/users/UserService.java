package uk.ac.ebi.biostd.exporter.jobs.users;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.persistence.dao.UsersDao;
import uk.ac.ebi.biostd.exporter.persistence.model.UserDropboxInfo;

@Slf4j
@Component
@AllArgsConstructor
public class UserService {

    private final UsersFoldersProperties properties;
    private final UsersDao usersDao;
    private final AtomicInteger count = new AtomicInteger(0);

    public void execute() {
        List<UserDropboxInfo> dropboxInfoList = usersDao.getUsersDropbox();
        dropboxInfoList.forEach(this::createSymLink);
        count.set(0);
    }

    private void createSymLink(UserDropboxInfo userDropbox) {
        File sourceFile = new File(getDropboxPath(userDropbox.getSecret()));
        sourceFile.mkdirs();

        try {
            File symLinkFile = new File(getSymLinkPath(userDropbox.getEmail()));
            symLinkFile.getParentFile().mkdirs();

            Path source = sourceFile.toPath();
            Path symLinkPath = symLinkFile.toPath();

            log.info("creating symbolic link {}, to {} in {}", count.getAndIncrement(), source, symLinkFile);
            Files.createSymbolicLink(symLinkPath, source);
        } catch (IOException e) {
            log.error("Could not create symbolic link path", e);
        }
    }

    private String getDropboxPath(String magicKey) {
        String prefixFolder = magicKey.substring(0, 2);
        String folder = magicKey.substring(2);

        return format("%s/%s/%s", properties.getBaseDropboxPath(), prefixFolder, folder);
    }

    private String getSymLinkPath(String userEmail) {
        String prefixFolder = userEmail.substring(0, 1);
        return format("%s/%s/%s", properties.getSymLinksPath(), prefixFolder, userEmail);
    }
}
