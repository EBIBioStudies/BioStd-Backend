package uk.ac.ebi.biostd.tools.convert;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import java.util.List;

public interface Config {

    @Unparsed
    List<String> getFiles();

    @Option(shortName = "i", defaultValue = "auto")
    String getInputFormat();

    @Option(shortName = "o", defaultValue = "xml")
    String getOutputFormat();

    @Option(shortName = "l", defaultValue = "-")
    String getLogFile();

    @Option(shortName = "d")
    boolean getPrintInfoNodes();

    @Option(shortName = "g")
    boolean getGenAcc();

    @Option(shortName = "c", defaultValue = "utf-8")
    String getCharset();

    @Option(helpRequest = true, shortName = "h")
    boolean getHelp();

}
