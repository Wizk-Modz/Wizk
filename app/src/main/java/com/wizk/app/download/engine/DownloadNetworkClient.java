package com.wizk.app.download.engine;

import com.wizk.app.download.model.Chunk;
import com.wizk.app.download.model.DownloadTask;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class DownloadNetworkClient {

    private static final int BUFFER_SIZE = 8192;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 30000;

    private final DownloadTask task;

    // Khởi tạo client mạng cho một tác vụ tải xuống
    public DownloadNetworkClient(DownloadTask task) {
        this.task = task;
    }

    // Kiểm tra dung lượng, MIME type và khả năng tải theo HTTP Range của máy chủ
    public boolean inspectServer() {
        HttpURLConnection connection = null;
        try {
            connection = openConnection("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                return false;
            }

            long contentLength = connection.getContentLengthLong();
            if (contentLength > 0) {
                task.setTotalSize(contentLength);
            }
            if (task.getMimeType() == null || task.getMimeType().isEmpty()) {
                task.setMimeType(connection.getContentType());
            }

            boolean supportsRange = "bytes".equalsIgnoreCase(connection.getHeaderField("Accept-Ranges"));
            task.setRangeSupported(supportsRange);
            return supportsRange;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Tải một phân đoạn dữ liệu vào vị trí tương ứng trong tệp đích
    public void downloadChunk(Chunk chunk, File destinationFile, BooleanSupplier isStopped) throws Exception {
        if (chunk.getDownloaded() >= chunk.getLength()) {
            return;
        }

        HttpURLConnection connection = null;
        try {
            connection = openConnection("GET");
            if (task.isRangeSupported() && chunk.getLength() < Long.MAX_VALUE) {
                connection.setRequestProperty("Range", "bytes=" + chunk.getBegin() + "-" + chunk.getEnd());
            }

            int responseCode = connection.getResponseCode();
            validateResponse(responseCode, chunk);

            try (InputStream inputStream = connection.getInputStream();
                 RandomAccessFile outputFile = new RandomAccessFile(destinationFile, "rw")) {
                outputFile.seek(chunk.getBegin());
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while (!isStopped.getAsBoolean() && (read = inputStream.read(buffer)) != -1) {
                    outputFile.write(buffer, 0, read);
                    chunk.setDownloaded(chunk.getDownloaded() + read);
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Tạo HTTP connection có timeout và header của phiên WebView
    private HttpURLConnection openConnection(String method) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(task.getUrl()).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        applyHeaders(connection);
        return connection;
    }

    // Xác nhận mã phản hồi máy chủ phù hợp với phương thức tải được dùng
    private void validateResponse(int responseCode, Chunk chunk) {
        boolean usesRange = task.isRangeSupported() && chunk.getLength() < Long.MAX_VALUE;
        if (usesRange && responseCode != HttpURLConnection.HTTP_PARTIAL) {
            throw new IllegalStateException("Máy chủ không phản hồi HTTP Range hợp lệ");
        }
        if (!usesRange && (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE)) {
            throw new IllegalStateException("Máy chủ trả về mã HTTP " + responseCode);
        }
    }

    // Gắn các HTTP header đã thu thập từ phiên WebView vào yêu cầu mạng
    private void applyHeaders(HttpURLConnection connection) {
        Map<String, String> headers = task.getHeaders();
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }
}
