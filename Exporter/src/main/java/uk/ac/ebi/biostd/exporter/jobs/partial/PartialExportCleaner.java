package uk.ac.ebi.biostd.exporter.jobs.partial;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.utils.FileUtil;

@Slf4j
@Component
@AllArgsConstructor
public class PartialExportCleaner {
    private final PartialExportJobProperties configProperties;

    public void execute() {
        log.info("cleaning up partial export files");
        FileUtil
            .listFilesMatching(configProperties.getFilePath(), configProperties.getFileName() + ".+")
            .forEach(File::delete);
    }
}
