package com.wizk.app.download;

import android.content.Context;
import android.webkit.DownloadListener;
import android.webkit.WebView;

import com.wizk.app.download.engine.DownloadUtils;
import com.wizk.app.download.model.DownloadRequest;
import com.wizk.app.download.ui.DownloadDialog;

public class BrowserDownloadListener implements DownloadListener {

    private final Context context;
    private final WebView webView;

    // Khởi tạo bộ lắng nghe tải xuống cho phiên WebView
    public BrowserDownloadListener(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    // Tạo yêu cầu tải xuống và hiển thị các phương thức tải khả dụng
    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
        if (!DownloadUtils.isSupportedDownloadUrl(url)) {
            return;
        }
        String fileName = DownloadUtils.guessFileName(url, contentDisposition, mimeType);
        DownloadRequest request = new DownloadRequest.Builder()
                .setUrl(url)
                .setFileName(fileName)
                .setUserAgent(userAgent)
                .setContentDisposition(contentDisposition)
                .setMimeType(mimeType)
                .setReferer(webView.getUrl())
                .setContentLength(contentLength)
                .build();
        DownloadDialog.show(context, request);
    }
}
