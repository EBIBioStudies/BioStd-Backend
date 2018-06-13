package uk.ac.ebi.biostd.webapp.application.rest.service;

import static uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto.fromSubmissionReport;
import static uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto.submitFailure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pri.util.Pair;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.db.AdHocTagResolver;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.ParserConfig;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.out.json.JSONFormatter;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitOperation;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.PTDocumentParser;

@Log4j
@Service
@AllArgsConstructor
public class SubmitService {

    private final JPASubmissionManager submissionManager;
    private final ObjectMapper objectMapper;

    public SubmitReportDto createOrUpdateSubmission(MultipartFile file, List<String> attachTo, SubmitOperation operation, User user) {
        if (file.isEmpty()) {
            return submitFailure("File '" + file.getOriginalFilename() + "' is empty");
        }

        Optional<DataFormat> format = autodetectDataFormat(file.getOriginalFilename());
        if (!format.isPresent()) {
            return submitFailure("Unrecognized data format");
        }

        try {
            return submit(file.getBytes(), attachTo, operation, format.get(), user);
        } catch (IOException e) {
            log.error("submit error", e);
            return submitFailure(e);
        }
    }

    public SubmitReportDto submit(byte[] data, List<String> attachTo, SubmitOperation op, DataFormat dataFormat, User user) throws IOException {
        JsonNode jsonNode;

        if (dataFormat == DataFormat.json) {
            jsonNode = objectMapper.readTree(data);
        } else {
            Pair<String, LogNode> converted = convertToJson(data, dataFormat);
            if (converted.getSecond().getLevel() == LogNode.Level.ERROR) {
                return SubmitReportDto.fromLogNode(converted.getSecond());
            }
            jsonNode = objectMapper.readTree(converted.getFirst());
        }

        // TODO: amendAttachTo(attachTo)
        return submitJson(jsonNode, op, user);
    }

    public SubmitReportDto submitJson(JsonNode jsonNode, SubmitOperation op, User user) {

        SubmissionManager.Operation operation = op == SubmitOperation.CREATE ? SubmissionManager.Operation.CREATE :
                SubmissionManager.Operation.UPDATE;

        SubmissionReport report = submissionManager.createSubmission(
                jsonNode.toString().getBytes(), DataFormat.json, "UTF-8", operation, user,
                false, false, null);
        SimpleLogNode.setLevels(report.getLog());
        return fromSubmissionReport(report);
    }

    private JsonNode amendAccno(JsonNode pageTab) {
        PageTabProxy pageTabProxy = new PageTabProxy(pageTab);
        List<String> attachToAccessions = pageTabProxy.attachToAttr();

        String accnoTemplate = attachToAccessions.size() == 1 ?
                getAccnoTemplate(attachToAccessions.get(0)) : DEFAULT_ACCNO_TEMPLATE;

        return pageTabProxy.amendAccno(accnoTemplate);
    }

    private String getAccnoTemplate(String parentAccno) {
        Submission subm = submissionManager.getSubmissionsByAccession(parentAccno);
        return subm.getAttributes().stream()
                .filter(attr -> attr.getName().equalsIgnoreCase("accnotemplate"))
                .map(AbstractAttribute::getValue)
                .findFirst()
                .orElse(DEFAULT_ACCNO_TEMPLATE);
    }

    @SneakyThrows
    private Pair<String, LogNode> convertToJson(byte[] data, DataFormat dataFormat) {
        ParserConfig pc = new ParserConfig();

        pc.setMultipleSubmissions(true);
        pc.setPreserveId(false);

        SimpleLogNode logNode = new SimpleLogNode(LogNode.Level.SUCCESS, "Converting " + dataFormat + " document", null);
        PMDoc doc = new PTDocumentParser(pc).parseDocument(data, dataFormat, "UTF-8", new AdHocTagResolver(), logNode);
        SimpleLogNode.setLevels(logNode);

        final StringWriter stringWriter = new StringWriter();
        new JSONFormatter(stringWriter, true).format(doc);
        return new Pair<>(stringWriter.toString(), logNode);
    }

    private Optional<DataFormat> autodetectDataFormat(String fileName) {
        Optional<String> fileExtension = Optional.ofNullable(fileName)
                .map(fn -> fn.split("\\."))
                .filter(array -> array.length > 0)
                .map(array -> array[array.length - 1]);

        if (!fileExtension.isPresent()) {
            return Optional.empty();
        }

        return Arrays.stream(DataFormat.values())
                .filter(v -> v.toString().equalsIgnoreCase(fileExtension.get()))
                .findFirst();
    }
}
