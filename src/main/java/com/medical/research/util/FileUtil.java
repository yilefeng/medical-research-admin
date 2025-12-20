package com.medical.research.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/20 19:19
 * @Description:
 */
public class FileUtil {
    public static final int BUFFER_SIZE = 4096;

    private static final String UTF8 = "UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final String GBK = "GBK";

    public static void downLoadFile(HttpServletResponse response, InputStream fileInputStream, String fileName, boolean open) throws IOException {
        if (fileInputStream == null) return;
        try (OutputStream outputStream = response.getOutputStream()) {
            response.setCharacterEncoding("utf-8");
            if (fileName.endsWith("zip")) {
                response.setContentType("application/x-zip-compressed");
            } else {
                response.setContentType("application/octet-stream");
            }
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, UTF8));
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = 0;
            while ((n = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, n);
            }
        } finally {
            fileInputStream.close();
        }
    }
}
