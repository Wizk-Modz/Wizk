package com.wizk.app;

import android.Manifest;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.wizk.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PermissionHelper permissionHelper = new PermissionHelper(this);

        permissionHelper.request(1002,Manifest.permission.POST_NOTIFICATIONS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
