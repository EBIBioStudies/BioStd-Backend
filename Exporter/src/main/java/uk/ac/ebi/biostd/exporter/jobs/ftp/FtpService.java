package uk.ac.ebi.biostd.exporter.jobs.ftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@Service
@AllArgsConstructor
@Slf4j
public class FtpService {
    private final FtpPublisherProperties properties;
    private final SubmissionDao submissionDao;
    private final AtomicInteger count = new AtomicInteger(0);

    public void execute() {
        submissionDao.getPublicSubmissionsPaths().forEach(this::generateLinks);
        finish();
    }

    public void execute(String accNo) {
        generateLinks(submissionDao.getSubmissionPathByAccNo(accNo));
        finish();
    }

    private void finish() {
        count.set(0);
        log.info("Finished copying all files");
    }

    private void generateLinks(String submissionRelPath) {
        deleteOutdatedLinks(submissionRelPath);
        createLink(submissionRelPath);
    }

    private void deleteOutdatedLinks(String submissionRelPath) {
        File outdatedFtpLinks = new File(properties.getBaseFtpPath() + "/" +submissionRelPath);

        if (outdatedFtpLinks.exists()) {
            try {
                FileUtils.deleteDirectory(outdatedFtpLinks);
            } catch (IOException exception) {
                log.error("Problem deleting outdated ftp links for {}", submissionRelPath, exception);
            }
        }
    }

    private void createLink(String submissionRelPath) {
        File submissionFiles = new File(properties.getBaseBioStudiesPath() + "/" + submissionRelPath + "/Files");
        if (submissionFiles.exists() && submissionFiles.listFiles() != null) {
            Stream.of(submissionFiles.listFiles()).parallel().forEach(file -> {
                try {
                    File ftpFile = new File(properties.getBaseFtpPath() + "/" + submissionRelPath + "/" + file.getName());
                    ftpFile.getParentFile().mkdirs();

                    Path source = file.toPath();
                    Path ftp = ftpFile.toPath();

                    log.info("creating hard link {}, to {} in {}", count.getAndIncrement(), source, ftp);
                    Files.createLink(ftp, source);
                } catch (IOException e) {
                    log.error("Could not create symbolic link path", e);
                }
            });
        }
    }
}
