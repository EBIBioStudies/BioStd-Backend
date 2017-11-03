package uk.ac.ebi.biostd.tools.convert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.ebi.biostd.db.AdHocTagResolver;
import uk.ac.ebi.biostd.db.TagResolver;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.ParserConfig;
import uk.ac.ebi.biostd.in.ParserException;
import uk.ac.ebi.biostd.in.pagetab.PageTabSyntaxParser;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.util.FileUtil;
import uk.ac.ebi.mg.spreadsheet.SpreadsheetReader;
import uk.ac.ebi.mg.spreadsheet.readers.CSVTSVSpreadsheetReader;

public class CVSTVSParse {

    public static PMDoc parse(File infile, String charset, char sep, SimpleLogNode topLn) {

        String text = null;

        try {
            Charset cs = null;

            if (charset != null) {
                try {
                    cs = Charset.forName(charset);
                } catch (Exception e) {
                    System.err.println("Invalid charset: '" + charset + "'");
                    return null;
                }
            } else {
                cs = Charset.forName("utf-8");
            }

            text = FileUtil.readFile(infile, cs);
        } catch (IOException e) {
            System.err.println("Input file read ERROR: " + e.getMessage());
            return null;
        }

        ParserConfig pc = new ParserConfig();
        pc.setMultipleSubmissions(true);
        TagResolver tr = new AdHocTagResolver();

        PageTabSyntaxParser prs = new PageTabSyntaxParser(tr, pc);

        SpreadsheetReader sp = new CSVTSVSpreadsheetReader(text, sep);

        try {
            return prs.parse(sp, topLn);
        } catch (ParserException e) {
            System.err.println("Can't read CSV/TSV file: " + infile.getAbsolutePath() + " Error: " + e.getMessage());
        }

        return null;
    }

}
