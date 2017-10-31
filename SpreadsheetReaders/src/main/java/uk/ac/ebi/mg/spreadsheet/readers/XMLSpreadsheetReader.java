/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.mg.spreadsheet.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import uk.ac.ebi.mg.spreadsheet.SpreadsheetReader;

public class XMLSpreadsheetReader implements SpreadsheetReader, ContentHandler {

    public static final String SPREADSHEET_URI = "urn:schemas-microsoft-com:office:spreadsheet";
    public static final String WORKSHEET = "Worksheet";
    public static final String TABLE = "Table";
    public static final String ROW = "Row";
    public static final String CELL = "Cell";
    public static final String DATA = "Data";
    public static final String CELLINDEX = "Index";

    private boolean inWorksheet = false;
    private boolean inTable = false;
    private boolean inRow = false;
    private boolean inCell = false;
    private boolean inData = false;
    private boolean hasDataInCell = false;

    private boolean worksheetPassed = false;

    private int col = 0;

    private List<List<String>> sheet = new ArrayList<List<String>>();

    private List<String> cRow;

    private int lineNo = 0;

    public XMLSpreadsheetReader(String txt) throws ParserConfigurationException, SAXException, IOException {
        this(new InputSource(new StringReader(txt)));
    }

    public XMLSpreadsheetReader(InputStream src) throws ParserConfigurationException, SAXException, IOException {
        this(new InputSource(src));
    }

    public XMLSpreadsheetReader(InputSource src) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        SAXParser saxParser = spf.newSAXParser();

        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);

        xmlReader.parse(src);

    }

    @Override
    public int getLineNumber() {
        return lineNo;
    }

    @Override
    public List<String> readRow(List<String> accum) {
        if (lineNo >= sheet.size()) {
            return null;
        }

        accum.clear();

        accum.addAll(sheet.get(lineNo));
        lineNo++;

        return accum;
    }

    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (!inData) {
            return;
        }

        if (col == cRow.size()) {
            cRow.set(col - 1, cRow.get(col - 1) + new String(arg0, arg1, arg2));
        } else {
            cRow.add(new String(arg0, arg1, arg2));
        }
    }

    @Override
    public void endDocument() throws SAXException {
        lineNo = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (worksheetPassed) {
            return;
        }

        if (WORKSHEET.equals(qName)) {
            inWorksheet = true;
        } else if (TABLE.equals(qName) && inWorksheet) {
            inTable = true;
        } else if (ROW.equals(qName) && inTable) {
            inRow = true;
            lineNo++;

            String ciStr = atts.getValue(SPREADSHEET_URI, CELLINDEX);

            if (ciStr != null) {
                try {
                    int n = Integer.parseInt(ciStr);

                    for (int i = lineNo; i < n; i++) {
                        sheet.add(Collections.<String>emptyList());
                        lineNo++;
                    }
                } catch (Exception e) {
                }
            }

            cRow = new ArrayList<String>();
            sheet.add(cRow);
            col = 0;
        } else if (CELL.equals(qName) && inRow) {
            inCell = true;
            hasDataInCell = false;

            col++;

//   for( int i=0; i < atts.getLength(); i++ )
//    System.out.println(atts.getLocalName(i)+"="+atts.getValue(i));

            String ciStr = atts.getValue(SPREADSHEET_URI, CELLINDEX);

            if (ciStr != null) {
                try {
                    int n = Integer.parseInt(ciStr);

                    for (int i = col; i < n; i++) {
                        cRow.add("");
                        col++;
                    }
                } catch (Exception e) {
                }
            }
        } else if (DATA.equals(qName) && inCell) {
            inData = true;
            hasDataInCell = true;
        }

    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (WORKSHEET.equals(qName)) {
            worksheetPassed = true;
            inWorksheet = false;
            inTable = false;
            inRow = false;
            inCell = false;
            inData = false;
        } else if (TABLE.equals(qName)) {
            inTable = false;
            inRow = false;
            inCell = false;
            inData = false;
        } else if (ROW.equals(qName)) {
            inRow = false;
            inCell = false;
            inData = false;
        } else if (CELL.equals(qName)) {
            inCell = false;
            inData = false;

            if (!hasDataInCell) {
                cRow.add("");
            }
        } else if (DATA.equals(qName) && inCell) {
            inData = false;
        }
    }

    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
    }

    @Override
    public void processingInstruction(String arg0, String arg1) throws SAXException {
    }

    @Override
    public void setDocumentLocator(Locator arg0) {
    }

    @Override
    public void skippedEntity(String arg0) throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String arg0, String arg1) throws SAXException {
    }

}
