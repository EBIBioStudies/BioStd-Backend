package uk.ac.ebi.biostd.exporter.jobs.ftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        submissionDao.getPublicSubmissionsPaths().forEach(this::createLink);
        count.set(0);
    }

    private void createLink(String submissionRelPath) {
        File submissionFiles = new File(properties.getBaseBioStudiesPath() + "/" + submissionRelPath + "/Files");
        if (submissionFiles.exists() && submissionFiles.listFiles() != null) {
            Stream.of(submissionFiles.listFiles()).forEach(file -> {
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
