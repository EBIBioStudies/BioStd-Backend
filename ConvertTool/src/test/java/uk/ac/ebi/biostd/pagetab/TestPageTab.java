package uk.ac.ebi.biostd.pagetab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.ParserConfig;
import uk.ac.ebi.biostd.in.ParserException;
import uk.ac.ebi.biostd.in.pagetab.PageTabSyntaxParser;
import uk.ac.ebi.biostd.in.pagetab.SectionOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.out.pageml.PageMLFormatter;
import uk.ac.ebi.biostd.treelog.ErrorCounter;
import uk.ac.ebi.biostd.treelog.ErrorCounterImpl;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.util.FileUtil;
import uk.ac.ebi.mg.spreadsheet.readers.CSVTSVSpreadsheetReader;

public class TestPageTab {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File in = new File("e:/dev/temp/dataUni_us.txt");
//  File in = new File("C:/Documents and Settings/Mike/My Documents/Upload/idgen.txt");

        FileInputStream fis = new FileInputStream(in);

        int first = fis.read();
        int second = fis.read();

        fis.close();

        Charset cs = Charset.defaultCharset();

        if ((first == 0xFF && second == 0xFE) || (first == 0xFE && second == 0xFF)) {
            cs = Charset.forName("UTF-16");
        }

        String text = FileUtil.readFile(in, cs);

        ParserConfig cfg = new ParserConfig();
        PageTabSyntaxParser pars = new PageTabSyntaxParser(null, cfg);

        ErrorCounter cnt = new ErrorCounterImpl();

        SimpleLogNode ln = new SimpleLogNode(Level.SUCCESS, "Processing Page-Tab file", cnt);

        PMDoc sbm = null;

        try {
            sbm = pars.parse(new CSVTSVSpreadsheetReader(text, '\0'), ln);
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (ln.getLevel() == Level.SUCCESS) {
            IdGen idGen = new IdGen();

            for (SubmissionInfo s : sbm.getSubmissions()) {
                if (s.getAccNoPrefix() != null || s.getAccNoSuffix() != null) {
                    s.getSubmission().setAccNo(
                            (s.getAccNoPrefix() != null ? s.getAccNoPrefix() : "") + idGen.getId() + (
                                    s.getAccNoSuffix() != null ? s.getAccNoSuffix() : ""));
                }

                for (SectionOccurrence sr : s.getGlobalSections()) {
                    String accNo =
                            (sr.getPrefix() != null ? sr.getPrefix() : "") + idGen.getId() + (sr.getSuffix() != null
                                    ? sr.getSuffix() : "");

                    sr.getSection().setAccNo(accNo);
                }
            }
        }

//  System.out.println(sbm.getAcc());

        printNode(ln, "");

        if (ln.getLevel() == Level.SUCCESS) {

            File xmlOut = new File(in.getParentFile(), "xmlout.xml");

            PageMLFormatter fmt = new PageMLFormatter();

            FileWriter out = new FileWriter(xmlOut);

            out.append("<data>\n");

            for (SubmissionInfo s : sbm.getSubmissions()) {
                fmt.format(s.getSubmission(), out);
            }

            out.append("</data>\n");

            out.close();

        }

    }

    private static void printNode(LogNode ln, String indent) {
        System.out.println(indent + ln.getLevel() + " " + ln.getMessage());

        if (ln.getSubNodes() == null) {
            return;
        }

        for (LogNode sln : ln.getSubNodes()) {
            printNode(sln, indent + "   ");
        }

    }

    static class IdGen {

        private int gen = 1;

        public int getId() {
            return gen++;
        }
    }

}
