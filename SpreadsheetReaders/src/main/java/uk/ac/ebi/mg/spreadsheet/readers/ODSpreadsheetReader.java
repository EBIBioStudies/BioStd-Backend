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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import uk.ac.ebi.mg.spreadsheet.SpreadsheetReader;

public class ODSpreadsheetReader implements SpreadsheetReader {

    private static DateFormat dateFormat;

    private int lineNo = 0;
    private int maxRow = 0;
    private SpreadsheetDocument document;
    private Table table;


    public ODSpreadsheetReader(SpreadsheetDocument doc) {
        document = doc;
        table = doc.getSheetByIndex(0);

        maxRow = table.getRowCount();
    }

    @Override
    public int getLineNumber() {
        return lineNo;
    }

    @Override
    public List<String> readRow(List<String> accum) {
        if (lineNo > maxRow) {
            return null;
        }

        accum.clear();

        Row r = table.getRowByIndex(lineNo++);

        if (r != null) {

            int lcn = r.getCellCount();

            for (int j = 0; j < lcn; j++) {
                Cell c = r.getCellByIndex(j);

                if (c != null) {
                    if ("date".equals(c.getValueType())) {
                        if (dateFormat == null) {
                            dateFormat = new SimpleDateFormat(dateTimeFormat);
                        }

                        accum.add(dateFormat.format(c.getDateValue().getTime()));
                    } else {
                        accum.add(c.getStringValue());
                    }
                } else {
                    accum.add("");
                }

            }

        }

        if (lineNo == maxRow) {
            document.close();
        }

        return accum;
    }

}
