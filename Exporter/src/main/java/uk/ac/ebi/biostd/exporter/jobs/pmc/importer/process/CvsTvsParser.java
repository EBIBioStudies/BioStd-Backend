package uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.db.AdHocTagResolver;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.ParserConfig;
import uk.ac.ebi.biostd.in.pagetab.PageTabSyntaxParser;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.util.FileUtil;
import uk.ac.ebi.mg.spreadsheet.readers.CSVTSVSpreadsheetReader;

@Component
public class CvsTvsParser {

    private static final String COMPRESS_FILE_EXTENSION = ".gz";

    private final PageTabSyntaxParser pageTabSyntaxParser;

    public CvsTvsParser() {
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.setMultipleSubmissions(true);

        pageTabSyntaxParser = new PageTabSyntaxParser(new AdHocTagResolver(), parserConfig);
    }

    @SneakyThrows
    PMDoc parseZipFile(File file, char sep, SimpleLogNode topLn) {
        String text = file.getName().endsWith(COMPRESS_FILE_EXTENSION) ?
                FileUtil.readGzFile(file, UTF_8) :
                FileUtil.readFile(file, UTF_8);
        return pageTabSyntaxParser.parse(new CSVTSVSpreadsheetReader(text, sep, false), topLn);
    }
}
