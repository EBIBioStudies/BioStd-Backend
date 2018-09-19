package uk.ac.ebi.biostd.exporter.jobs.users;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.commons.files.MagicFolderUtil;
import uk.ac.ebi.biostd.exporter.persistence.dao.UsersDao;
import uk.ac.ebi.biostd.exporter.persistence.model.UserDropboxInfo;

@Slf4j
@Component
@AllArgsConstructor
public class UserService {

    private final UsersFoldersProperties properties;
    private final UsersDao usersDao;
    private final AtomicInteger count = new AtomicInteger(0);
    private final MagicFolderUtil magicFolderUtil;

    public void execute() {
        List<UserDropboxInfo> dropboxInfoList = usersDao.getUsersDropbox();
        dropboxInfoList.forEach(this::createSymLink);
        count.set(0);
    }

    private void createSymLink(UserDropboxInfo userDropbox) {
        Path source = magicFolderUtil.getUserMagicFolderPath(userDropbox.getId(), userDropbox.getSecret());
        magicFolderUtil.createUserSymlink(source, userDropbox.getEmail());
    }
}
