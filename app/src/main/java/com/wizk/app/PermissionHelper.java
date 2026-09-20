package com.wizk.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class PermissionHelper {

    private final Activity activity;
    private final ActivityResultLauncher<String[]> launcher;

    private int requestCode;
    private String[] requestedPermissions;

    private final Map<String, Boolean> results = new HashMap<>();

    // Khởi tạo trình quản lý quyền và đăng ký bộ xử lý kết quả
    public PermissionHelper(Activity activity) {
        this.activity = activity;

        launcher = ((androidx.activity.ComponentActivity) activity)
                .registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {

                            results.clear();
                            results.putAll(result);

                            boolean allGranted = true;

                            for (Boolean granted : result.values()) {
                                if (!Boolean.TRUE.equals(granted)) {
                                    allGranted = false;
                                    break;
                                }
                            }

                            onResult(requestCode, allGranted);
                        }
                );
    }

    // Yêu cầu cấp quyền hoặc trả kết quả ngay nếu đã được cấp
    public void request(int requestCode, @NonNull String... permissions) {

        this.requestCode = requestCode;
        this.requestedPermissions = permissions;

        boolean allGranted = true;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
            ) != PackageManager.PERMISSION_GRANTED) {

                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            onResult(requestCode, true);
            return;
        }

        launcher.launch(permissions);
    }

    // Xử lý kết quả cấp quyền từ hệ thống
    protected void onResult(int requestCode, boolean allGranted) {
        // Override nếu cần xử lý kết quả
    }

    // Kiểm tra xem một quyền cụ thể đã được cấp hay chưa
    public boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                activity,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }
}