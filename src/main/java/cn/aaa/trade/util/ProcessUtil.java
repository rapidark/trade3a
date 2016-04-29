package cn.aaa.trade.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtil {
    private static Logger logger = LoggerFactory.getLogger(ProcessUtil.class);

    public static String run(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        readProcessOutput(process, out, err);
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return out.toString();
        } else {
            throw new Exception(err.toString());
        }
    }

    private static void readProcessOutput(final Process process, StringBuilder out, StringBuilder err) throws IOException {
        read(process.getInputStream(), out);
        read(process.getErrorStream(), err);
    }

    private static void read(InputStream inputStream, StringBuilder out) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, PropertiesUtil.get("outputEncode", "gb2312")));
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
