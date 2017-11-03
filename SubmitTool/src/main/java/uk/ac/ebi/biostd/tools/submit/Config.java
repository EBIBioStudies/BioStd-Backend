package uk.ac.ebi.biostd.tools.submit;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import java.util.List;

public interface Config {

    @Unparsed
    List<String> getFiles();

    @Option(shortName = "i", defaultValue = "auto")
    String getInputFormat();

    @Option(shortName = "u")
    String getUser();

    @Option(shortName = "p", maximum = 1)
    List<String> getPassword();

    boolean isPassword();

    @Option(shortName = "s")
    String getServer();

    @Option(shortName = "l", defaultValue = "-")
    String getLogFile();

    @Option(shortName = "d")
    boolean getPrintInfoNodes();

    @Option(shortName = "o", defaultValue = "new")
    String getOperation();

    @Option(shortName = "c", defaultValue = "utf-8")
    String getCharset();

    @Option(shortName = "v")
    boolean getValidateOnly();

    @Option(shortName = "b", maximum = 1)
    List<String> getOnBehalf();

    boolean isOnBehalf();

    @Option
    public boolean getIgnoreAbsentFiles();

    @Option(shortName = "m", maximum = 1)
    public List<String> getMappingFile();

    public boolean isMappingFile();

    @Option(helpRequest = true, shortName = "h")
    boolean getHelp();

}
