package uk.ac.ebi.biostd.webapp.application.rest.service;

import static java.lang.String.format;
import static uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pri.util.collection.Collections;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.PTDocumentParser;

@Slf4j
@Service
@AllArgsConstructor
public class SubmitService {

    private static final String DEFAULT_ACCNO_TEMPLATE = "!{S-BSST}";

    private final JPASubmissionManager submissionManager;
    private final ObjectMapper objectMapper;

    public SubmitReportDto createOrUpdateSubmission(MultipartFile file, List<String> projectAccNumbers,
            String accnoTemplate, SubmitOperation operation, User user) {
        if (file.isEmpty()) {
            return fromErrorMessage(format("File %s is empty", file.getOriginalFilename()));
        }

        Optional<DataFormat> format = DataFormat.fromFileNameOrContentType(
                file.getOriginalFilename(),
                file.getContentType());
        if (!format.isPresent()) {
            return fromErrorMessage(format("Unrecognized data format: %s, %s", file.getOriginalFilename(), file.getContentType()));
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("An error in getBytes()", e);
            return fromErrorMessage(e.getMessage());
        }
        return submit(bytes, format.get(), projectAccNumbers, accnoTemplate, operation, user);
    }

    public SubmitReportDto submit(byte[] data, DataFormat dataFormat, List<String> projectAccNumbers,
            String accnoTemplate, SubmitOperation operation, User user) {
        return convertToJson(data, dataFormat)
                .map(jsonNode -> submitJson(jsonNode, projectAccNumbers, accnoTemplate, operation, user))
                .complete((resultLog, errorLog) -> Optional.ofNullable(resultLog).orElse(errorLog));
    }

    public SubmitReportDto submitJson(JsonNode jsonNode, SubmitOperation operation, User user) {
        return submitJson(jsonNode, Collections.emptyList(), DEFAULT_ACCNO_TEMPLATE, operation, user);
    }

    private SubmitReportDto submitJson(JsonNode jsonNode, List<String> projectAccNumbers, String accnoTemplate,
            SubmitOperation operation, User user) {

        jsonNode = amendJson(jsonNode, projectAccNumbers, accnoTemplate);

        SubmissionReport report = submissionManager.createSubmission(
                asMultipleSubmissions(jsonNode).toString().getBytes(), DataFormat.json, "UTF-8", operation.toLegacyOp(), user,
                false, false, null);
        SimpleLogNode.setLevels(report.getLog());
        return fromSubmissionReport(report);
    }

    private JsonNode amendJson(JsonNode pageTab, List<String> projectAccNumbers, String accnoTemplate) {
        return Optional.of(new PageTabProxy(pageTab))
                .map(proxy -> proxy.addAttachToAttr(projectAccNumbers, objectMapper))
                .map(proxy -> proxy.setAccnoIfEmpty(getAccnoTemplate(proxy.getAttachToAttr(),
                        Optional.ofNullable(accnoTemplate).orElse(DEFAULT_ACCNO_TEMPLATE))))
                .map(PageTabProxy::json)
                .get();
    }

    private String getAccnoTemplate(Set<String> projectAccNumbers, String dflt) {
        if (projectAccNumbers.size() != 1) {
            return dflt;
        }
        Submission subm = submissionManager.getSubmissionsByAccession(projectAccNumbers.iterator().next());
        return subm.getAttributes().stream()
                .filter(attr -> attr.getName().equalsIgnoreCase("accnotemplate"))
                .map(AbstractAttribute::getValue)
                .findFirst()
                .orElse(dflt);
    }

    private Result<JsonNode, SubmitReportDto> convertToJson(byte[] data, DataFormat dataFormat) {
        try {
            if (dataFormat == DataFormat.json) {
                return Result.success(objectMapper.readTree(data));
            }

            ParserConfig pc = new ParserConfig();

            pc.setMultipleSubmissions(true);
            pc.setPreserveId(false);

            SimpleLogNode logNode = new SimpleLogNode(LogNode.Level.SUCCESS, "Converting " + dataFormat + " document", null);
            PMDoc doc = new PTDocumentParser(pc).parseDocument(data, dataFormat, "UTF-8", new AdHocTagResolver(), logNode);
            SimpleLogNode.setLevels(logNode);

            if (logNode.getLevel() == LogNode.Level.ERROR) {
                return Result.error(fromLogNode(logNode));
            }

            final StringWriter stringWriter = new StringWriter();
            new JSONFormatter(stringWriter, true).format(doc);

            return Result.success(objectMapper.readTree(data));
        } catch (IOException e) {
            return Result.error(fromErrorMessage(e.getMessage()));
        }
    }

    private JsonNode asMultipleSubmissions(JsonNode pageTab) {
        return new PageTabProxy(pageTab).wrappedJson(objectMapper);
    }

    private interface Result<R, E> {

        <T> Result<T, E> map(Function<R, T> f);

        <T> Result<R, T> error(Function<E, T> f);

        <T> T complete(BiFunction<R, E, T> f);

        static <R, E> Result<R, E> success(R t) {
            return new SuccessResult<>(t);
        }

        static <R, E> Result<R, E> error(E e) {
            return new ErrorResult<>(e);
        }
    }

    private static class SuccessResult<R, E> implements Result<R, E> {
        private R result;

        SuccessResult(R result) {
            this.result = result;
        }

        @Override
        public <T> Result<T, E> map(Function<R, T> f) {
            return new SuccessResult<>(f.apply(result));
        }

        @Override
        public <T> Result<R, T> error(Function<E, T> f) {
            return new SuccessResult<>(result);
        }

        @Override
        public <T> T complete(BiFunction<R, E, T> f) {
            return f.apply(result, null);
        }
    }

    private static class ErrorResult<R, E> implements Result<R, E> {
        private E error;

        ErrorResult(E error) {
            this.error = error;
        }

        @Override
        public <T> Result<T, E> map(Function<R, T> f) {
            return new ErrorResult<>(error);
        }

        @Override
        public <T> Result<R, T> error(Function<E, T> f) {
            return new ErrorResult<>(f.apply(error));
        }

        @Override
        public <T> T complete(BiFunction<R, E, T> f) {
            return f.apply(null, error);
        }
    }
}
