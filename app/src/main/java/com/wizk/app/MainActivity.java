package com.wizk.app;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
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
        
        webView.setWebChromeClient(new WebChromeClient() {
            
        });
        
        webView.setWebViewClient(new WebViewClient() {
            
        });
        
        ws.setMediaPlaybackRequiresUserGesture(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setBuiltInZoomControls(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setLoadWithOverviewMode(true);
        ws.setLoadsImagesAutomatically(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setJavaScriptEnabled(true);
        ws.setGeolocationEnabled(true);
        ws.setDefaultTextEncodingName("utf-8");
        ws.setDisplayZoomControls(false);
        ws.setDomStorageEnabled(true);
        ws.setSafeBrowsingEnabled(true);
        ws.setSupportMultipleWindows(true);
        ws.setSupportZoom(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccess(true);
        ws.setUseWideViewPort(true);
        ws.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36");
        
        binding.wizkbtn.setOnClickListener(v -> {
            webView.reload();
        });
        
        Greeting gng = new Greeting();
        
        Toast.makeText(MainActivity.this,gng.Greeting(),Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onBackPressed() {
        if(binding.wizkwebview.canGoBack()) {
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
