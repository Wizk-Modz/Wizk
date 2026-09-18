package com.wizk.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wizk.app.databinding.ActivityMainBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> fileChooserLauncher;
    private ValueCallback<Uri[]> filePathCallback;
    private boolean isLoading = false;

    // Khởi tạo Activity và cấu hình toàn bộ thành phần của trình duyệt
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFileChooser();
        setupWebView();
        setupViews();
        setupBackNavigation();

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState);
        } else {
            loadUrlOrSearch(getString(R.string.default_homepage));
        }
    }

    // Đăng ký bộ xử lý kết quả chọn tệp tải lên từ hệ thống
    private void initFileChooser() {
        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (filePathCallback == null) {
                        return;
                    }
                    Uri[] results = null;
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            results = new Uri[count];
                            for (int i = 0; i < count; i++) {
                                results[i] = data.getClipData().getItemAt(i).getUri();
                            }
                        } else if (data.getData() != null) {
                            results = new Uri[]{data.getData()};
                        }
                    }
                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
        );
    }

    // Cấu hình cài đặt, sự kiện điều hướng và tính năng tải tệp của WebView
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        binding.webView.setWebViewClient(new WebViewClient() {
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
                    startActivity(intent);
                } catch (Exception ignored) {
                }
                return true;
            }

            // Xử lý cập nhật giao diện khi bắt đầu tải trang web
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isLoading = true;
                binding.btnAction.setImageResource(R.drawable.ic_close);
                binding.btnAction.setContentDescription(getString(R.string.btn_stop));
                binding.progressIndicator.setVisibility(View.VISIBLE);
                if (!binding.editUrl.hasFocus()) {
                    binding.editUrl.setText(url);
                }
                updateNavButtons();
            }

            // Xử lý cập nhật giao diện và trạng thái điều hướng khi trang web tải xong
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isLoading = false;
                binding.btnAction.setImageResource(R.drawable.ic_refresh);
                binding.btnAction.setContentDescription(getString(R.string.btn_refresh));
                binding.progressIndicator.setVisibility(View.GONE);
                if (!binding.editUrl.hasFocus()) {
                    binding.editUrl.setText(url);
                }
                updateNavButtons();
            }

            // Thông báo lỗi khi không thể tải trang web
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                binding.progressIndicator.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, getString(R.string.error_loading_page), Toast.LENGTH_SHORT).show();
            }
        });

        binding.webView.setWebChromeClient(new WebChromeClient() {
            // Đồng bộ tiến độ tải trang lên thanh hiển thị tiến trình
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                binding.progressIndicator.setProgressCompat(newProgress, true);
                if (newProgress >= 100) {
                    binding.progressIndicator.setVisibility(View.GONE);
                } else {
                    binding.progressIndicator.setVisibility(View.VISIBLE);
                }
            }

            // Xử lý sự kiện mở hộp thoại chọn tệp tải lên từ trang web
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    fileChooserLauncher.launch(intent);
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        binding.webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription(getString(R.string.download_started, ""));
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                request.setTitle(fileName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, fileName);

                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(MainActivity.this, getString(R.string.download_started, fileName), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thiết lập sự kiện người dùng cho các nút điều hướng và thanh nhập URL
    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack();
            }
        });

        binding.btnForward.setOnClickListener(v -> {
            if (binding.webView.canGoForward()) {
                binding.webView.goForward();
            }
        });

        binding.btnAction.setOnClickListener(v -> {
            if (isLoading) {
                binding.webView.stopLoading();
            } else {
                binding.webView.reload();
            }
        });

        binding.btnHome.setOnClickListener(v -> loadUrlOrSearch(getString(R.string.default_homepage)));

        binding.editUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String input = binding.editUrl.getText().toString().trim();
                loadUrlOrSearch(input);
                hideKeyboard(binding.editUrl);
                binding.editUrl.clearFocus();
                return true;
            }
            return false;
        });

        updateNavButtons();
    }

    // Thiết lập xử lý cử chỉ hoặc nút quay lại của hệ thống
    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            // Lùi trang trong lịch sử WebView nếu có hoặc thoát Activity
            @Override
            public void handleOnBackPressed() {
                if (binding != null && binding.webView.canGoBack()) {
                    binding.webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // Điều hướng tới URL chỉ định hoặc tìm kiếm trên công cụ tìm kiếm
    private void loadUrlOrSearch(String input) {
        if (input == null || input.isEmpty()) {
            return;
        }

        if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("file://") || input.startsWith("about:")) {
            binding.webView.loadUrl(input);
            return;
        }

        if (!input.contains(" ") && (Patterns.WEB_URL.matcher(input).matches() || input.contains("."))) {
            binding.webView.loadUrl("https://" + input);
            return;
        }

        try {
            String encodedQuery = URLEncoder.encode(input, "UTF-8");
            String searchUrl = String.format(getString(R.string.search_engine_url), encodedQuery);
            binding.webView.loadUrl(searchUrl);
        } catch (UnsupportedEncodingException e) {
            binding.webView.loadUrl("https://www.google.com/search?q=" + input);
        }
    }

    // Cập nhật trạng thái hiển thị và kích hoạt của nút Back và Forward
    private void updateNavButtons() {
        if (binding == null) {
            return;
        }
        boolean canGoBack = binding.webView.canGoBack();
        boolean canGoForward = binding.webView.canGoForward();

        binding.btnBack.setEnabled(canGoBack);
        binding.btnBack.setAlpha(canGoBack ? 1.0f : 0.4f);

        binding.btnForward.setEnabled(canGoForward);
        binding.btnForward.setAlpha(canGoForward ? 1.0f : 0.4f);
    }

    // Ẩn bàn phím ảo của hệ thống sau khi hoàn tất nhập liệu
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Lưu trạng thái của WebView khi có thay đổi cấu hình
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            binding.webView.saveState(outState);
        }
    }

    // Phục hồi hoạt động của WebView khi Activity vào lại tiền cảnh
    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) {
            binding.webView.onResume();
        }
    }

    // Tạm dừng hoạt động của WebView khi Activity rời khỏi tiền cảnh
    @Override
    protected void onPause() {
        if (binding != null) {
            binding.webView.onPause();
        }
        super.onPause();
    }

    // Hủy WebView và giải phóng tài nguyên khi Activity bị đóng
    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.webView.stopLoading();
            binding.webView.destroy();
            binding = null;
        }
        super.onDestroy();
    }
}
