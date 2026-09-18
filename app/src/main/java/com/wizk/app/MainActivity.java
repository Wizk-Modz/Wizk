package com.wizk.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wizk.app.client.BrowserWebChromeClient;
import com.wizk.app.client.BrowserWebViewClient;
import com.wizk.app.databinding.ActivityMainBinding;
import com.wizk.app.download.BrowserDownloadListener;
import com.wizk.app.util.UrlUtils;

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
            loadUrl(getString(R.string.default_homepage));
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

    // Cấu hình WebView, cài đặt thuộc tính và gắn các bộ lắng nghe sự kiện
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

        binding.webView.setWebViewClient(new BrowserWebViewClient(this, new BrowserWebViewClient.BrowserWebCallback() {
            @Override
            public void onPageStarted(String url) {
                isLoading = true;
                binding.btnAction.setImageResource(R.drawable.ic_close);
                binding.btnAction.setContentDescription(getString(R.string.btn_stop));
                binding.progressIndicator.setVisibility(View.VISIBLE);
                if (!binding.editUrl.hasFocus()) {
                    binding.editUrl.setText(url);
                }
                updateNavButtons();
            }

            @Override
            public void onPageFinished(String url) {
                isLoading = false;
                binding.btnAction.setImageResource(R.drawable.ic_refresh);
                binding.btnAction.setContentDescription(getString(R.string.btn_refresh));
                binding.progressIndicator.setVisibility(View.GONE);
                if (!binding.editUrl.hasFocus()) {
                    binding.editUrl.setText(url);
                }
                updateNavButtons();
            }

            @Override
            public void onPageError(String failingUrl) {
                binding.progressIndicator.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, getString(R.string.error_loading_page), Toast.LENGTH_SHORT).show();
            }
        }));

        binding.webView.setWebChromeClient(new BrowserWebChromeClient(new BrowserWebChromeClient.BrowserChromeCallback() {
            @Override
            public void onProgressChanged(int newProgress) {
                binding.progressIndicator.setProgressCompat(newProgress, true);
                binding.progressIndicator.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(ValueCallback<Uri[]> callback, BrowserWebChromeClient.FileChooserParams params) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;
                try {
                    fileChooserLauncher.launch(params.createIntent());
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
            }
        }));

        binding.webView.setDownloadListener(new BrowserDownloadListener(this, new BrowserDownloadListener.DownloadCallback() {
            @Override
            public void onDownloadStarted(String fileName) {
                Toast.makeText(MainActivity.this, getString(R.string.download_started, fileName), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadFailed(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }));
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

        binding.btnHome.setOnClickListener(v -> loadUrl(getString(R.string.default_homepage)));

        binding.editUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String input = binding.editUrl.getText().toString().trim();
                loadUrl(input);
                UrlUtils.hideKeyboard(MainActivity.this, binding.editUrl);
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

    // Điều hướng WebView đến liên kết web hoặc truy vấn tìm kiếm
    private void loadUrl(String input) {
        String targetUrl = UrlUtils.formatUrlOrSearch(input, getString(R.string.search_engine_url));
        if (!targetUrl.isEmpty()) {
            binding.webView.loadUrl(targetUrl);
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
