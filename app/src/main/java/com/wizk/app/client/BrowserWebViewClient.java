package com.wizk.app.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowserWebViewClient extends WebViewClient {

    public interface BrowserWebCallback {
        void onPageStarted(String url);
        void onPageFinished(String url);
        void onPageError(String failingUrl);
    }

    private final Context context;
    private final BrowserWebCallback callback;

    // Khởi tạo BrowserWebViewClient với ngữ cảnh và bộ lắng nghe sự kiện
    public BrowserWebViewClient(Context context, BrowserWebCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    // Kiểm tra và chuyển hướng liên kết web hoặc ứng dụng ngoài an toàn
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return false;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
        return true;
    }

    // Xử lý sự kiện khi bắt đầu tải trang web
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (callback != null) {
            callback.onPageStarted(url);
        }
    }

    // Xử lý sự kiện khi trang web tải xong hoàn tất
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (callback != null) {
            callback.onPageFinished(url);
        }
    }

    // Xử lý và chuyển tiếp thông báo lỗi tải trang web
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (callback != null) {
            callback.onPageError(failingUrl);
        }
    }
}
