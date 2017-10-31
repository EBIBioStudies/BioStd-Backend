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

package uk.ac.ebi.biostd.in.pagetab;


public class CellValue {

    private byte[] rbMarks;
    private String value;
    private String rawValue;

    private int row;
    private int col;

    public CellValue(String val) {
        this(val, null, 0, 0);
    }

    public CellValue(String val, String escSeq) {
        this(val, escSeq, 0, 0);
    }

    public CellValue(String val, String escSeq, int r, int c) {
        rawValue = val;

        row = r;
        col = c;

        if (escSeq == null) {
            value = val;
            return;
        }

        int len = val.length();
        StringBuilder sb = null;

        int ptr = 0;

        while (ptr < len) {
            int pos = val.indexOf(escSeq, ptr);

            if (pos != -1) {
                if (sb == null) {
                    sb = new StringBuilder(len);
                    rbMarks = new byte[len];
                }

                rbMarks[sb.length() + pos - ptr] = 1;

                sb.append(val.substring(ptr, pos));

            } else {
                if (sb != null) {
                    sb.append(val.substring(ptr));
                }

                break;
            }

            ptr = pos + 1;
        }

        if (sb != null) {
            value = sb.toString().trim();
        } else {
            value = val.trim();
        }

    }


    public String getValue() {
        return value;
    }

    public byte[] getRbMarks() {
        return rbMarks;
    }

    public boolean matchString(String substr) {
        if (substr.length() != value.length()) {
            return false;
        }

        return matchSubstring(substr, 0);
    }

    public boolean matchSubstring(String substr, int offs) {
        if (!value.regionMatches(offs, substr, 0, substr.length())) {
            return false;
        }

        if (rbMarks != null) {
            for (int i = offs; i < offs + substr.length(); i++) {
                if (rbMarks[i] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isSymbolRed(int i) {
        if (rbMarks == null) {
            return false;
        }

        return rbMarks[i] != 0;
    }

    public boolean hasRed(int beg, int end) {
        if (rbMarks != null) {
            for (int i = beg; i < end; i++) {
                if (rbMarks[i] != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public void trim() {
        int len = value.length();
        int st = 0;

        while ((st < len) && (value.charAt(st) <= ' ')) {
            st++;
        }

        while ((st < len) && (value.charAt(len - 1) <= ' ')) {
            len--;
        }

        if (st == 0 && len == value.length()) {
            return;
        }

        value = value.substring(st, len);

        if (rbMarks == null || st == 0) {
            return;
        }

        for (int i = 0; i < value.length(); i++) {
            rbMarks[i] = rbMarks[i + st];
        }
    }

    public String getRawValue() {
        return rawValue;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
