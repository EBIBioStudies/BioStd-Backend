package uk.ac.ebi.biostd.exporter.jobs.pmc.importer;

import com.google.common.collect.Streams;
import java.io.File;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.CvsTvsParser;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.PmcFileManager;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.SubmissionJsonSerializer;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.remote.service.RemoteService;
import uk.ac.ebi.biostd.treelog.ErrorCounterImpl;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;

@Component
@AllArgsConstructor
public class PmcImporter {

    private final PmcImportProperties properties;
    private final CvsTvsParser cvsTvsParser;
    private final PmcFileManager submissionManager;
    private final RemoteService remoteService;
    private final SubmissionJsonSerializer jsonSerializer;

    public void execute() {
        String sessionId = remoteService.login(properties.getUser(), properties.getPassword()).getSessid();
        Streams.stream(FileUtils.iterateFiles(new File(properties.getImportPath()), null, true))
                .map(this::parseSubmissionFile)
                .flatMap(List::stream)
                .forEach(sub -> submitSubmission(sessionId, sub));
    }

    private List<SubmissionInfo> parseSubmissionFile(File file) {
        SimpleLogNode topLn = new SimpleLogNode(Level.SUCCESS, "Parsing file: ", new ErrorCounterImpl());
        return cvsTvsParser.parse(file, '\t', topLn).getSubmissions();
    }

    private void submitSubmission(String sessionId, SubmissionInfo subInfo) {
        submissionManager.downloadFiles(subInfo);
        jsonSerializer.getJson(subInfo).ifPresent(jsonBody -> remoteService.createJsonSubmission(sessionId, jsonBody));
    }
}
