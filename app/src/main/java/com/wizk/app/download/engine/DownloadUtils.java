package com.wizk.app.download.engine;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DownloadUtils {

    private static final String TAG = "DownloadUtils";
    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            Pattern.compile("attachment;\\s*filename\\*?=\\s*(?:UTF-8''|\")?([^;\"\\r\\n]*)", Pattern.CASE_INSENSITIVE);

    // Ngăn chặn khởi tạo thể hiện của lớp tiện ích
    private DownloadUtils() {
    }

    // Kiểm tra URL có phải HTTP hoặc HTTPS hợp lệ để tải xuống hay không
    public static boolean isSupportedDownloadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    // Dự đoán tên tệp từ URL, tiêu đề phản hồi và loại MIME
    public static String guessFileName(String url, String contentDisposition, String mimeType) {
        String filename = null;

        if (!TextUtils.isEmpty(contentDisposition)) {
            filename = parseContentDisposition(contentDisposition);
        }

        if (TextUtils.isEmpty(filename) && !TextUtils.isEmpty(url)) {
            try {
                String decodedUrl = URLDecoder.decode(url, "UTF-8");
                int queryIndex = decodedUrl.indexOf('?');
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                int slashIndex = decodedUrl.lastIndexOf('/');
                if (slashIndex >= 0 && slashIndex < decodedUrl.length() - 1) {
                    filename = decodedUrl.substring(slashIndex + 1);
                }
            } catch (Exception ignored) {
            }
        }

        if (TextUtils.isEmpty(filename)) {
            filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
        }

        if (filename != null && !filename.contains(".") && !TextUtils.isEmpty(mimeType)) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!TextUtils.isEmpty(extension)) {
                filename = filename + "." + extension;
            }
        }

        return sanitizeFileName(filename);
    }

    // Loại bỏ ký tự đường dẫn và tên đặc biệt khỏi tên tệp tải xuống
    public static String sanitizeFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "downloadfile.bin";
        }
        String sanitizedName = new File(fileName).getName().replace('\0', '_');
        if (sanitizedName.isEmpty() || ".".equals(sanitizedName) || "..".equals(sanitizedName)) {
            return "downloadfile.bin";
        }
        return sanitizedName;
    }

    // Trích xuất tên tệp từ tiêu đề Content-Disposition
    public static String parseContentDisposition(String contentDisposition) {
        if (TextUtils.isEmpty(contentDisposition)) {
            return null;
        }
        try {
            Matcher matcher = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (matcher.find()) {
                String rawName = matcher.group(1);
                if (rawName != null) {
                    rawName = rawName.trim();
                    if (rawName.endsWith("\"")) {
                        rawName = rawName.substring(0, rawName.length() - 1);
                    }
                    try {
                        return URLDecoder.decode(rawName, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        return rawName;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi phân tích Content-Disposition: " + contentDisposition, e);
        }
        return null;
    }

    // Tạo tệp duy nhất không trùng tên trong thư mục đích
    public static File getUniqueFile(File dir, String fileName) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File targetFile = new File(dir, fileName);
        if (!targetFile.exists()) {
            return targetFile;
        }

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int counter = 1;
        while (targetFile.exists()) {
            targetFile = new File(dir, baseName + " (" + counter + ")" + extension);
            counter++;
        }
        return targetFile;
    }

    // Lấy thư mục tải xuống riêng tương thích Scoped Storage của ứng dụng
    public static File getDefaultDownloadDir(Context context) {
        File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        return downloadDir != null ? downloadDir : new File(context.getFilesDir(), Environment.DIRECTORY_DOWNLOADS);
    }

    // Định dạng số byte thành chuỗi dung lượng thân thiện
    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    // Cập nhật tệp tải về vào thư viện MediaStore của hệ thống
    public static void scanFile(Context context, File file, String mimeType) {
        if (context == null || file == null || !file.exists()) {
            return;
        }
        try {
            MediaScannerConnection.scanFile(
                    context.getApplicationContext(),
                    new String[]{file.getAbsolutePath()},
                    mimeType != null ? new String[]{mimeType} : null,
                    null
            );
        } catch (Exception e) {
            Log.e(TAG, "Lỗi quét tệp vào MediaStore", e);
        }
    }
}
