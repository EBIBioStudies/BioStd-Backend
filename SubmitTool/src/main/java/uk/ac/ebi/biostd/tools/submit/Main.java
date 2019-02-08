package uk.ac.ebi.biostd.tools.submit;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.InvalidOptionSpecificationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import uk.ac.ebi.biostd.in.AccessionMapping;
import uk.ac.ebi.biostd.in.SubmissionMapping;
import uk.ac.ebi.biostd.treelog.ConvertException;
import uk.ac.ebi.biostd.treelog.JSON4Log;
import uk.ac.ebi.biostd.treelog.JSON4Report;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.treelog.Utils;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.util.FileUtil;
import uk.ac.ebi.biostd.util.StringUtils;

public class Main {

    private static final String SessionKey = "BIOSTDSESS";
    private static final String authEndpoint = "auth/signin";
    private static final String endpointPfx = "submit/";

    public static void main(String[] args) {
        Config config = null;

        try {
            config = CliFactory.parseArguments(Config.class, args);
        } catch (InvalidOptionSpecificationException | ArgumentValidationException e) {
            usage();
            System.exit(1);
        }

        if (config.getFiles() == null || config.getFiles().size() < 1 || config.getFiles().size() > 2) {
            System.err.println("Command line processing ERROR: invalid number of files specified");
            usage();
            System.exit(1);
        }

        File infile = null;
        Operation op = null;

        String pi = config.getOperation();

        if (pi != null) {
            for (Operation o : Operation.values()) {
                if (o.name().equalsIgnoreCase(pi)) {
                    op = o;
                    break;
                }
            }
        }

        if (op == null) {
            System.err.println("Invalid operation. Valid are: " + Arrays.asList(Operation.values()));
            System.exit(1);
        }

        String endPoint = null;
        Prm[] params = null;

        if (op == Operation.delete || op == Operation.remove) {
            clParams(config, 1);
            endPoint = endpointPfx + op.name();
            params = new Prm[]{new Prm("id", config.getFiles().get(0))};
        } else if (op == Operation.tranklucate) {
            clParams(config, 1);
            endPoint = endpointPfx + op.name();
            params = new Prm[]{new Prm("accno", config.getFiles().get(0))};
        } else if (op == Operation.tranklucate_by_pattern) {
            clParams(config, 1);
            endPoint = endpointPfx + Operation.tranklucate.name();
            params = new Prm[]{new Prm("accnoPattern", config.getFiles().get(0))};
        } else if (op == Operation.chown || op == Operation.chown_by_pattern) {
            clParams(config, 2);
            endPoint = endpointPfx + Operation.chown.name();
            params = new Prm[]{new Prm("owner", config.getFiles().get(1)),
                    new Prm(op == Operation.chown ? "accno" : "accnoPattern", config.getFiles().get(0))};
        }

        if (endPoint != null) {
            String sess = login(config);
            LogNode topLn = genReq(endPoint, params, sess, config);
            printLog(topLn, config);
            return;
        } else {
            infile = new File(config.getFiles().get(0));
        }

        if (!infile.canRead()) {
            System.err.println("Input file '" + infile.getAbsolutePath() + "' not exist or not readable");
            usage();
            System.exit(1);
        }

        DataFormat fmt = null;

        if ("auto".equalsIgnoreCase(config.getInputFormat())) {
            String ext = null;

            int pos = infile.getName().lastIndexOf('.');

            if (pos >= 0) {
                ext = infile.getName().substring(pos + 1);
            }

            if ("xlsx".equalsIgnoreCase(ext)) {
                fmt = DataFormat.xlsx;
            } else if ("xls".equalsIgnoreCase(ext)) {
                fmt = DataFormat.xls;
            } else if ("json".equalsIgnoreCase(ext)) {
                fmt = DataFormat.json;
            } else if ("ods".equalsIgnoreCase(ext)) {
                fmt = DataFormat.ods;
            } else if ("csv".equalsIgnoreCase(ext)) {
                fmt = DataFormat.csv;
            } else if ("tsv".equalsIgnoreCase(ext)) {
                fmt = DataFormat.tsv;
            } else {
                fmt = DataFormat.csvtsv;
            }
        } else {
            try {
                fmt = DataFormat.valueOf(config.getInputFormat());
            } catch (Exception e) {
                System.err.println("Invalid input format: '" + config.getInputFormat() + "'");
                System.exit(1);
            }
        }

        String sess = login(config);

        String obUser = null;

        if (config.getOnBehalf() != null && config.getOnBehalf().size() > 0) {
            obUser = config.getOnBehalf().get(0);
        }

        SubmissionReport report = submit(infile, fmt, sess, config, op,
                config.getValidateOnly(),
                config.getIgnoreAbsentFiles(),
                obUser);

        LogNode topLn = report.getLog();

        if (config.getMappingFile() != null && topLn.getLevel().getPriority() < Level.ERROR.getPriority()) {
            printMappings(report.getMappings(), config);
        }

        printLog(topLn, config);

        System.exit((topLn != null && topLn.getLevel().getPriority() < Level.ERROR.getPriority()) ? 0 : 2);
    }

    private static void clParams(Config cfg, int n) {
        if (cfg.getFiles().size() != n) {
            System.err.println("Invalid number of parameters: " + n + " expected");
            System.exit(1);
        }
    }

    private static void printMappings(List<SubmissionMapping> mappings, Config config) {
        PrintStream out = System.out;

        if (config.getMappingFile().size() > 0) {
            File lf = new File(config.getMappingFile().get(0));

            if (lf.exists() && !lf.canWrite()) {
                System.err.println("Mapping file '" + config.getMappingFile() + "' is not writable");
                System.exit(1);
            }

            try {
                out = new PrintStream(lf, "UTF-8");
            } catch (FileNotFoundException e) {
                System.err.println("Can't open mapping file '" + config.getMappingFile() + "'");
                System.exit(1);
            } catch (UnsupportedEncodingException e) {
                System.err.println("UTF-8 encoding is not supported");
                System.exit(1);
            }

        }

        for (SubmissionMapping smp : mappings) {
            AccessionMapping saccm = smp.getSubmissionMapping();

            String assAcc = saccm.getAssignedAcc();

            if (assAcc == null || assAcc.length() == 0) {
                assAcc = saccm.getOrigAcc();
            }

            out.println("Submission " + saccm.getPosition()[0] + " : " + saccm.getOrigAcc() + " -> " + assAcc);

            if (smp.getSectionsMapping() != null) {
                for (AccessionMapping secm : smp.getSectionsMapping()) {
                    out.print("  Section subm[");

                    boolean first = true;
                    for (int n : secm.getPosition()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print("]/sec[");
                        }

                        out.print(n);
                    }

                    assAcc = secm.getAssignedAcc();

                    if (assAcc == null || assAcc.length() == 0) {
                        assAcc = secm.getOrigAcc();
                    }

                    out.println("] : " + secm.getOrigAcc() + " -> " + assAcc);
                }
            }
        }

        if (out != System.out) {
            out.close();
        }

    }

    private static LogNode genReq(String epoint, Prm[] prms, String sess, Config config) {
        StringBuilder sb = new StringBuilder();

        sb.append(config.getServer());

        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }

        URL loginURL = null;

        try {
            sb.append(epoint).append('?').append(SessionKey).append('=').append(URLEncoder.encode(sess, "utf-8"));

            for (Prm pr : prms) {
                sb.append('&').append(pr.name).append('=').append(URLEncoder.encode(pr.value, "utf-8"));
            }

            loginURL = new URL(sb.toString());
        } catch (MalformedURLException e) {
            System.err.println("Invalid server URL: " + config.getServer());
            System.exit(1);
        } catch (UnsupportedEncodingException e) {
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) loginURL.openConnection();

            String resp = StringUtils.readFully((InputStream) conn.getContent(), Charset.forName("utf-8"));

            conn.disconnect();

            try {
                return JSON4Log.convert(resp);
            } catch (ConvertException e) {
                System.err.println("Invalid server response. JSON log expected");
                System.exit(1);
            }


        } catch (IOException e) {
            System.err.println("Connection to server '" + config.getServer() + "' failed: " + e.getMessage());
            System.exit(1);
        }

        return null;
    }

    private static LogNode genDelete(String delAccNo, String epoint, String param, String sess, Config config) {
        String appUrl = config.getServer();

        if (!appUrl.endsWith("/")) {
            appUrl = appUrl + "/";
        }

        URL loginURL = null;

        try {
            loginURL = new URL(
                    appUrl + epoint + "?" + param + "=" + URLEncoder.encode(delAccNo, "utf-8") + "&" + SessionKey + "="
                            + URLEncoder.encode(sess, "utf-8"));
        } catch (MalformedURLException e) {
            System.err.println("Invalid server URL: " + config.getServer());
            System.exit(1);
        } catch (UnsupportedEncodingException e) {
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) loginURL.openConnection();

            String resp = StringUtils.readFully((InputStream) conn.getContent(), Charset.forName("utf-8"));

            conn.disconnect();

            try {
                return JSON4Log.convert(resp);
            } catch (ConvertException e) {
                System.err.println("Invalid server response. JSON log expected");
                System.exit(1);
            }


        } catch (IOException e) {
            System.err.println("Connection to server '" + config.getServer() + "' failed: " + e.getMessage());
            System.exit(1);
        }

        return null;
    }

    private static SubmissionReport submit(File infile, DataFormat fmt, String sess, Config config, Operation op,
            boolean validateOnly, boolean ignAbsFiles, String onBeh) {
        StringBuilder urlsb = new StringBuilder();

        String appUrl = config.getServer();

        urlsb.append(appUrl);

        if (!appUrl.endsWith("/")) {
            urlsb.append("/");
        }

        urlsb.append(endpointPfx).append(op.name());

        try {
            urlsb.append("?").append(SessionKey).append("=").append(URLEncoder.encode(sess, "utf-8"));

            if (onBeh != null) {
                urlsb.append("&onBehalf=").append(URLEncoder.encode(onBeh, "utf-8"));
            }
        } catch (UnsupportedEncodingException e1) {
        }

        if (validateOnly) {
            urlsb.append("&validateOnly=true");
        }

        if (ignAbsFiles) {
            urlsb.append("&ignoreAbsentFiles=true");
        }

        URL loginURL = null;

        try {
            loginURL = new URL(urlsb.toString());
        } catch (MalformedURLException e) {
            System.err.println("Invalid server URL: " + config.getServer());
            System.exit(1);
        }

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) loginURL.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            if (fmt == DataFormat.json || fmt == DataFormat.csv || fmt == DataFormat.tsv || fmt == DataFormat.csvtsv) {
                conn.setRequestProperty("Content-Type", fmt.getContentType() + "; charset=utf-8");

                String text = null;

                try {
                    if ("auto".equalsIgnoreCase(config.getCharset())) {
                        text = FileUtil.readUnicodeFile(infile);
                    } else {
                        Charset cs = null;

                        try {
                            cs = Charset.forName(config.getCharset());
                        } catch (Throwable t) {
                            System.err.println("Invalid charset: " + config.getCharset());
                            System.exit(1);
                        }

                        text = FileUtil.readFile(infile, cs);
                    }
                } catch (IOException e) {
                    System.err.println("Input file read ERROR: " + e.getMessage());
                    System.exit(1);
                }

                byte[] postData = text.getBytes(Charset.forName("UTF-8"));

                conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
                conn.getOutputStream().write(postData);
                conn.getOutputStream().close();

            } else {
                conn.setRequestProperty("Content-Type", fmt.getContentType());

                byte[] data = FileUtil.readBinFile(infile);

                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                conn.getOutputStream().write(data);
                conn.getOutputStream().close();
            }

            String resp = StringUtils.readFully((InputStream) conn.getContent(), Charset.forName("utf-8"));

            conn.disconnect();

            try {
                return JSON4Report.convert(resp);
            } catch (ConvertException e) {
                System.err.println("Invalid server response. JSON log expected");
                System.exit(1);
            }


        } catch (IOException e) {
            if (conn != null) {
                try {
                    String resp = StringUtils.readFully(conn.getErrorStream(), Charset.forName("utf-8"));

                    if (resp.startsWith("FAIL ")) {
                        System.err.println("ERROR: " + resp.substring(5));
                        System.exit(1);
                    }
                } catch (Exception e2) {
                }
            }

            System.err.println("Connection to server '" + config.getServer() + "' failed: " + e.getMessage());
            System.exit(1);
        }

        return null;
    }

    private static String login(Config config) {
        String appUrl = config.getServer();

        if (!appUrl.endsWith("/")) {
            appUrl = appUrl + "/";
        }

        URL loginURL = null;

        String password = "";

        if (config.getPassword() != null) {
            if (config.getPassword().size() > 0) {
                password = config.getPassword().get(0);
            } else {
                try {
                    password = new String(System.console().readPassword("Password for %s: ", config.getUser()));
                } catch (IOError e) {
                    System.err.println("Can't read password from input stream: " + e.getMessage());
                    System.exit(1);
                }
            }
        }

        try {
            loginURL = new URL(appUrl + authEndpoint);
        } catch (MalformedURLException e) {
            System.err.println("Invalid server URL: " + config.getServer());
            System.exit(1);
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) loginURL.openConnection();
            String body = String.format("{ \"login\": \"%s\", \"password\", \"%s\" }", config.getUser(), password);

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write(body.getBytes());


            System.out.println("Response" + conn.getResponseCode() + " " + conn.getResponseMessage());

            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    JSONObject response = new JSONObject(conn.getInputStream());
                    conn.disconnect();

                    return response.getString("sessid");

                case HttpURLConnection.HTTP_FORBIDDEN:
                    System.err.println("Auth failed: invalid user/password");
                    System.exit(1);

                default:
                    System.err.println("Login failed: " + conn.getResponseMessage());
                    System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Connection to server '" + config.getServer() + "' failed: " + e.getMessage());
            System.exit(1);
        }

        return null;
    }

    static void printLog(LogNode topLn, Config config) {
        if (topLn == null) {
            return;
        }

        SimpleLogNode.setLevels(topLn);

        if (topLn.getLevel() != Level.SUCCESS || config.getPrintInfoNodes()) {
            PrintStream out = null;

            if (config.getLogFile().equals("-")) {
                out = System.err;
            } else {
                File lf = new File(config.getLogFile());

                if (lf.exists() && !lf.canWrite()) {
                    System.err.println("Log file '" + config.getLogFile() + "' is not writable");
                    System.exit(1);
                }

                try {
                    out = new PrintStream(lf, "UTF-8");
                } catch (FileNotFoundException e) {
                    System.err.println("Can't open log file '" + config.getLogFile() + "'");
                    System.exit(1);
                } catch (UnsupportedEncodingException e) {
                    System.err.println("UTF-8 encoding is not supported");
                    System.exit(1);
                }

            }

            try {
                Utils.printLog(topLn, out, config.getPrintInfoNodes() ? Level.DEBUG : Level.WARN);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (out != System.err) {
                out.close();
            }
        }
    }

    static void usage() {
        System.err.println(
                "Usage: java -jar PTSubmit -o <operation> -s serverURL -u user -p [password] [-h] [-i in fmt] [-c "
                        + "charset] [-d] [-l logfile] [-m [mpFile]] <input file|AccNo> [owner]");
        System.err.println("-h or --help print this help message");
        System.err.println(
                "-i or --inputFormat input file format. Can be json,tsv,csv,xls,xlsx,ods. Default is auto (by file "
                        + "extension)");
        System.err.println("-c or --charset file charset (for text files only)");
        System.err.println("-s or --server server endpoint URL");
        System.err.println("-u or --user user login");
        System.err.println("-p or --password user password");
        System.err.println("-b or --onBehalf <user> request operation on behalf of other user");
        System.err.println(
                "-o or --operation requested operation. Can be: create, createupdate, update, override, "
                        + "createoverride, delete, remove, tranklucate, tranklucate_by_pattern,chown,chown_by_pattern");
        System.err.println("-d or --printInfoNodes print info messages along with errors and warnings");
        System.err.println("-l or --logFile defines log file. By default stdout");
        System.err.println("-m or --mappingFile print mapping file. By default print to stdout");
        System.err
                .println("-v or --verifyOnly simulate submission on the server side without actual database changing");
        System.err.println("--ignoreAbsentFiles ignore absent files. Only for testing purposes!");
        System.err.println(
                "<input file> PagaTab input file. Supported UCS-2 (UTF-16), UTF-8 CSV or TSV, XLS, XLSX, ODS, JSON "
                        + "(Or accession number for delete operation)");
        System.err.println("[owner] new owner for chown and chown_by_pattern operations");

    }

    enum Operation {
        create,
        createupdate,
        update,
        override,
        createoverride,
        delete,
        remove,
        tranklucate,
        tranklucate_by_pattern,
        chown,
        chown_by_pattern
    }

    public static class Prm {

        public String name;
        public String value;

        public Prm() {
        }

        public Prm(String n, String v) {
            name = n;
            value = v;
        }
    }
}
