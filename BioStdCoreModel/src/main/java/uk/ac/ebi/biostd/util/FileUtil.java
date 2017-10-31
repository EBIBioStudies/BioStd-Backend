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

package uk.ac.ebi.biostd.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;


public class FileUtil {

    public static String readFile(File f) throws IOException {
        return readFile(f, Charset.defaultCharset());
    }

    public static String readStream(Reader fis) throws IOException {
        StringBuilder sb = new StringBuilder();

        char[] buff = new char[64 * 1024];

        int n;
        while ((n = fis.read(buff)) > 0) {
            sb.append(buff, 0, n);
        }

        return sb.toString();
    }


    public static String readStream(InputStream fis, Charset chst, long sz) throws IOException {
        ByteArrayOutputStream baos = null;

        if (sz > 0 && sz < Integer.MAX_VALUE) {
            baos = new ByteArrayOutputStream((int) sz);
        } else {
            baos = new ByteArrayOutputStream();
        }

        try {
            byte[] buff = new byte[64 * 1024];
            int n;

            while ((n = fis.read(buff)) != -1) {
                baos.write(buff, 0, n);
            }

            buff = baos.toByteArray();

            int offs = 0;
            if ((buff[0] == (byte) 0xEF && buff[1] == (byte) 0xBB && buff[2] == (byte) 0xBF) && chst.displayName()
                    .equalsIgnoreCase("UTF-8")) {
                offs = 3;
            }

            return new String(buff, offs, buff.length - offs, chst);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }

            baos.close();
        }

    }


    public static String readFile(File f, Charset chst) throws IOException {
        FileInputStream fis = new FileInputStream(f);

        return readStream(fis, chst, f.length());
    }

    public static String readGzFile(File f, Charset chst) throws IOException {
        InputStream fis = new GZIPInputStream(new FileInputStream(f));

        return readStream(fis, chst, -1);
    }

    public static byte[] readBinFile(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) f.length());
        try {
            byte[] buff = new byte[64 * 1024];
            int n;

            while ((n = fis.read(buff)) != -1) {
                baos.write(buff, 0, n);
            }

            return baos.toByteArray();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }

            baos.close();
        }

    }

    public static void copyFile(File inf, File outf) throws IOException {
        byte[] buf = new byte[64 * 1024];

        try (InputStream fis = new FileInputStream(inf); OutputStream fos = new FileOutputStream(outf)) {
            int nread;
            while ((nread = fis.read(buf)) > 0) {
                fos.write(buf, 0, nread);
            }
        }


    }

    public static String readUnicodeFile(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) f.length());
        try {
            byte[] buff = new byte[4096];
            int n;
            int read = 0;

            do {
                n = fis.read(buff, read, buff.length - read);

                if (n == -1) {
                    if (read == 0) {
                        return "";
                    }

                    return new String(buff, 0, 1);
                }

                read += n;
            } while (read < 3);

            Charset cs = null;

            int offs = 0;

            if ((buff[0] == (byte) 0xFF && buff[1] == (byte) 0xFE) || (buff[0] == (byte) 0xFE
                    && buff[1] == (byte) 0xFF)) {
                cs = Charset.forName("UTF-16");
            } else if (buff[0] == (byte) 0xEF && buff[1] == (byte) 0xBB && buff[2] == (byte) 0xBF) {
                cs = Charset.forName("UTF-8");
                offs = 3;
            } else {
                cs = Charset.forName("UTF-8");
            }

            baos.write(buff, offs, read - offs);

            while ((n = fis.read(buff)) != -1) {
                baos.write(buff, 0, n);
            }

            return new String(baos.toByteArray(), cs);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }

            baos.close();
        }
    }

    public static void copyDirectory(File inDir, File outDir) throws IOException {
        if (!inDir.isDirectory()) {
            return;
        }

        for (File f : inDir.listFiles()) {
            File outFile = new File(outDir, f.getName());

            if (f.isDirectory()) {
                outFile.mkdirs();
                copyDirectory(f, outFile);
            } else {
                copyFile(f, outFile);
            }
        }

    }
}
