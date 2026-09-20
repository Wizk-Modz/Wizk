package com.wizk.app.client;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class BrowserWebChromeClient extends WebChromeClient {

    public interface BrowserChromeCallback {
        void onProgressChanged(int newProgress);
        boolean onShowFileChooser(ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams);
    }

    private final BrowserChromeCallback callback;

    // Khởi tạo BrowserWebChromeClient với bộ lắng nghe sự kiện Chrome
    public BrowserWebChromeClient(BrowserChromeCallback callback) {
        this.callback = callback;
    }

    // Cập nhật tiến độ tải trang web khi có thay đổi
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (callback != null) {
            callback.onProgressChanged(newProgress);
        }
    }

    // Chuyển tiếp yêu cầu chọn tệp tải lên từ trang web
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (callback != null) {
            return callback.onShowFileChooser(filePathCallback, fileChooserParams);
        }
        return false;
    }
}
