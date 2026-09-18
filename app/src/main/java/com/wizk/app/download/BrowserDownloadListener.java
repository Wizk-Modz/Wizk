package com.wizk.app.download;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

public class BrowserDownloadListener implements DownloadListener {

    public interface DownloadCallback {
        void onDownloadStarted(String fileName);
        void onDownloadFailed(String message);
    }

    private final Context context;
    private final DownloadCallback callback;

    // Khởi tạo bộ xử lý tải tệp với ngữ cảnh và callback thông báo
    public BrowserDownloadListener(Context context, DownloadCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    // Khởi động tiến trình tải tệp qua DownloadManager của hệ thống
    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimeType);
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            request.addRequestHeader("User-Agent", userAgent);

            String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
            request.setTitle(fileName);
            request.setDescription(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                if (callback != null) {
                    callback.onDownloadStarted(fileName);
                }
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onDownloadFailed(e.getMessage());
            }
        }
    }
}
