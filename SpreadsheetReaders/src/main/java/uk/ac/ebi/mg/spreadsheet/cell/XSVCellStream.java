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

package uk.ac.ebi.mg.spreadsheet.cell;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import uk.ac.ebi.mg.spreadsheet.CellStream;

public class XSVCellStream implements CellStream {

    private final static String dateFotmat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private Appendable stream;

    private boolean newLine;
    private char sep;
    private DateFormat dateFmt;

    public XSVCellStream(Appendable s, char sep) {
        stream = s;

        newLine = true;

        this.sep = sep;
    }

    public static CellStream getCSVCellStream(Appendable s) {
        return new XSVCellStream(s, ',');
    }

    public static CellStream getTSVCellStream(Appendable s) {
        return new XSVCellStream(s, '\t');
    }

    @Override
    public void addDateCell(long ts) throws IOException {
        if (dateFmt == null) {
            dateFmt = new SimpleDateFormat(dateFotmat);
        }

        addCell(dateFmt.format(new Date(ts)));

    }

    @Override
    public void addCell(String cont) throws IOException {
        if (newLine) {
            newLine = false;
        } else {
            stream.append(sep);
        }

        int ptr = 0;
        int pos = cont.indexOf('"');

        if (pos == -1) {
            stream.append(cont);
        } else {
            stream.append('"');

            while (pos != -1) {
                stream.append(cont.substring(ptr, pos + 1));
                stream.append('"');

                ptr = pos + 1;

                if (ptr >= cont.length()) {
                    break;
                }

                pos = cont.indexOf('"', ptr);
            }

            stream.append(cont.substring(ptr));
            stream.append('"');

        }

    }

    @Override
    public void nextCell() throws IOException {
        stream.append(sep);
    }

    @Override
    public void nextRow() throws IOException {
        stream.append('\n');
        newLine = true;
    }


    @Override
    public void start() {
    }

    @Override
    public void finish() {
    }

}
