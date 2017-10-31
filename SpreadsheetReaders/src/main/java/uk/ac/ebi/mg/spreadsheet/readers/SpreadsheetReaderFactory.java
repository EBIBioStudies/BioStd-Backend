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

import com.pri.util.stream.StreamPump;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.odftoolkit.simple.SpreadsheetDocument;
import uk.ac.ebi.mg.spreadsheet.SpreadsheetReader;

public class SpreadsheetReaderFactory {

    public static SpreadsheetReader getReader(byte[] data) {
        try {
            return getReader(new ByteArrayInputStream(data));
        } catch (IOException e) {
        }

        return null;
    }

    public static SpreadsheetReader getReader(InputStream data) throws IOException {
        return getReader(data, null);
    }

    public static SpreadsheetReader getReader(InputStream data, String fileName) throws IOException {
        String ext = null;

        if (fileName != null) {
            int pos = fileName.lastIndexOf('.');

            if (pos >= 0) {
                ext = fileName.substring(pos + 1).toLowerCase();
            }
        }

        if (ext == null || ext.equals("xls") || ext.equals("xlsx")) {
            try {
                Workbook wb = WorkbookFactory.create(data);

                return new XLSpreadsheetReader(wb);
            } catch (Throwable t) {
            }

            data.reset();
        }

        if (ext == null || ext.equals("ods")) {
            try {
                SpreadsheetDocument doc = SpreadsheetDocument.loadDocument(data);

                return new ODSpreadsheetReader(doc);
            } catch (Throwable e) {
            }

            data.reset();
        }

        if (ext == null || ext.equals("xml")) {
            try {
                return new XMLSpreadsheetReader(data);
            } catch (Throwable e) {
            }

            data.reset();
        }

        ByteArrayOutputStream bais = new ByteArrayOutputStream();

        StreamPump.doPump(data, bais, false);

        bais.close();

        byte[] barr = bais.toByteArray();

        int offs = 0;
        Charset cs = null;

        if (barr.length > 2 && ((barr[0] == (byte) 0xFF && barr[1] == (byte) 0xFE) || (barr[0] == (byte) 0xFE
                && barr[1] == (byte) 0xFF))) {
            cs = Charset.forName("UTF-16");
        } else if (barr.length > 2 && barr[0] == (byte) 0xEF && barr[1] == (byte) 0xBB && barr[2] == (byte) 0xBF) {
            cs = Charset.forName("UTF-8");
            offs = 3;
        } else {
            cs = Charset.forName("UTF-8");
        }

        return new CSVTSVSpreadsheetReader(new String(barr, offs, barr.length - offs, cs), '\0');
    }
}
