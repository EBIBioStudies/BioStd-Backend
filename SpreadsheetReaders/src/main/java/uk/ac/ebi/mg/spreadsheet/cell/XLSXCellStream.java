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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.ac.ebi.mg.spreadsheet.CellStream;

public class XLSXCellStream implements CellStream {

    private File file;
    private Workbook wb;
    private Sheet sheet;
    private Row row;

    private int rowNum;
    private int cellNum;

    private CellStyle dateStyle;

    public XLSXCellStream(File f) {
        file = f;
    }

    @Override
    public void addCell(String cont) throws IOException {
        Cell cell = row.createCell(cellNum++);
        cell.setCellValue(cont);
    }

    @Override
    public void addDateCell(long ts) throws IOException {
        if (dateStyle == null) {
            dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("m/d/yy"));
        }

        Cell cell = row.createCell(cellNum++);
        cell.setCellValue(new Date(ts));
        cell.setCellStyle(dateStyle);

    }

    @Override
    public void nextCell() throws IOException {
        cellNum++;
    }

    @Override
    public void nextRow() throws IOException {
        rowNum++;
        row = sheet.createRow(rowNum);
        cellNum = 0;
    }

    @Override
    public void start() throws IOException {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet("PageTab");
        rowNum = 0;
        row = sheet.createRow(rowNum);
        cellNum = 0;
    }

    @Override
    public void finish() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }

}
