package uk.ac.ebi.biostd.xmlreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import uk.ac.ebi.biostd.util.FileUtil;
import uk.ac.ebi.mg.spreadsheet.readers.XMLSpreadsheetReader;

public class XMLReader {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String txt = FileUtil.readFile(new File("c:/Dev/data/PTsample.xml"));

        XMLSpreadsheetReader rd = new XMLSpreadsheetReader(txt);

        List<String> row = new ArrayList<String>();

        while (rd.readRow(row) != null) {
            System.out.println(String.valueOf(rd.getLineNumber()) + ": " + row);
            row.clear();
        }

    }

}
