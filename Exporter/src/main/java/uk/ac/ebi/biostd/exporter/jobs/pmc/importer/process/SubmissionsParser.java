package uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process;

import static java.lang.String.format;
import static uk.ac.ebi.biostd.model.Submission.canonicReleaseDateAttribute;

import com.google.common.collect.Lists;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.SectionAttribute;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.treelog.ErrorCounterImpl;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;

@Slf4j
@AllArgsConstructor
@Component
public class SubmissionsParser {

    public static final String PUB_DATE_PARSING_ERROR = "Did not find year for submission '%s' in publication date "
            + "'%s'";

    private final CvsTvsParser cvsTvsParser;

    public List<SubmissionInfo> parseSubmissionFile(File file) {
        SimpleLogNode topLn = new SimpleLogNode(Level.SUCCESS, "Parsing file: ", new ErrorCounterImpl());
        List<SubmissionInfo> submissions = cvsTvsParser.parseZipFile(file, '\t', topLn).getSubmissions();
        return removeDuplicated(submissions);
    }

    private List<SubmissionInfo> removeDuplicated(List<SubmissionInfo> submissions) {
        Set<String> submissionSet = new HashSet<>();
        List<SubmissionInfo> uniqueSubmissions = new ArrayList<>();

        for (SubmissionInfo info : Lists.reverse(submissions)) {
            if (isNew(submissionSet, info.getAccNoOriginal())) {
                uniqueSubmissions.add(info);
                submissionSet.add(info.getAccNoOriginal());
            }
        }

        return setReleaseDate(uniqueSubmissions);
    }

    @SneakyThrows
    private List<SubmissionInfo> setReleaseDate(List<SubmissionInfo> submissions) {
        for (SubmissionInfo submissionInfo : submissions) {
            Submission sub = submissionInfo.getSubmission();

            for (Section section : sub.getRootSection().getSections()) {
                if (!section.getType().equalsIgnoreCase("Publication")) {
                    continue;
                }

                for (SectionAttribute attr : section.getAttributes()) {
                    if (attr.getName().equalsIgnoreCase("Publication date")) {
                        sub.addAttribute(canonicReleaseDateAttribute, tryToParse(attr.getValue(), sub.getAccNo()));
                    }
                }
            }
        }

        return submissions;
    }

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");

    private String tryToParse(String date, String accNo) {
        Matcher matcher = YEAR_PATTERN.matcher(date);
        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group());
            return LocalDateTime.from(LocalDate.ofYearDay(year, 1).atTime(0, 0)).toString();
        }

        throw new RuntimeException(format(PUB_DATE_PARSING_ERROR, date, accNo));
    }

    private boolean isNew(Set<String> submissionSet, String accNo) {
        return !submissionSet.contains(accNo);
    }
}
