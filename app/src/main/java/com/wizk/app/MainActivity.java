package com.wizk.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.wizk.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.wizkwebview.loadUrl("http://127.0.0.1:80/");

        WebView webView = binding.wizkwebview;
        WebSettings ws = webView.getSettings();

        ws.setUseWideViewPort(true);
        ws.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36");
        ws.setAlgorithmicDarkeningAllowed(true);
        ws.setAllowContentAccess(true);
        ws.setAllowContentAccess(true);
        ws.setSafeBrowsingEnabled(true);
        ws.setSupportMultipleWindows(true);
        ws.setSupportZoom(true);
        ws.setDefaultTextEncodingName("utf-8");
        ws.setDisplayZoomControls(false);
        ws.setDomStorageEnabled(true);
        ws.setGeolocationEnabled(true);
        ws.setJavaScriptEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setLoadWithOverviewMode(true);
        ws.setLoadsImagesAutomatically(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setBuiltInZoomControls(true);
        ws.setMediaPlaybackRequiresUserGesture(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        binding.wizkwebview.setWebChromeClient(new WebChromeClient() {
                    private View mCustomView;
                    private CustomViewCallback mCustomViewCallback;
                    private int mOriginalOrientation;
                    private int mOriginalSystemUiVisibility;

                    @Override
                    public Bitmap getDefaultVideoPoster() {
                        return null;
                    }

                    @Override
                    public void onShowCustomView(View view, CustomViewCallback callback) {
                        if (mCustomView != null) {
                            onHideCustomView();
                            return;
                        }
                        mCustomView = view;
                        mCustomViewCallback = callback;
                        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                        mOriginalSystemUiVisibility = decor.getSystemUiVisibility();
                        mOriginalOrientation = getRequestedOrientation();
                        decor.addView(mCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                        hideSystemUI();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    }

                    @Override
                    public void onHideCustomView() {
                        if (mCustomView == null) return;
                        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                        decor.removeView(mCustomView);
                        mCustomView = null;
                        showSystemUI();
                        setRequestedOrientation(mOriginalOrientation);
                        if (mCustomViewCallback != null) {
                            mCustomViewCallback.onCustomViewHidden();
                            mCustomViewCallback = null;
                        }
                    }

                    private void hideSystemUI() {
                        Window window = getWindow();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            window.setDecorFitsSystemWindows(false);
                            WindowInsetsController controller = window.getInsetsController();
                            if (controller != null) {controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                            }
                        } else {
                            View decorView = window.getDecorView();decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                        }
                    }

                    private void showSystemUI() {
                        Window window = getWindow();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            window.setDecorFitsSystemWindows(true);
                            WindowInsetsController controller = window.getInsetsController();
                            if (controller != null) {
                                controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                            }
                        } else {
                            View decorView = window.getDecorView();
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        }
                    }
                });
                
        binding.wizkwebview.setWebViewClient(new WebViewClient() {});
        
        binding.fab.setOnClickListener(v -> {
            webView.reload();
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.wizkwebview.canGoBack()) {
            binding.wizkwebview.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
