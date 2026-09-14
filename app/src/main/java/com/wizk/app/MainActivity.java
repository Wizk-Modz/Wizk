package com.wizk.app;

import android.app.Activity;
import android.os.Bundle;
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
        binding.textView.setText("SetVN!");
        
        binding.textView.setOnClickListener(view -> {
            Toast.makeText(this, "SetVN!", Toast.LENGTH_SHORT).show();
        });
    }

    // @Override
    // public void onBackPressed() {
    //      finish();
    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
