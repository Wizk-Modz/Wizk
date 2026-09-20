package com.wizk.app.download.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadReceiver extends BroadcastReceiver {

    // Nhận sự kiện tương tác từ thông báo và chuyển tiếp đến DownloadService
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Intent serviceIntent = new Intent(context.getApplicationContext(), DownloadService.class);
        serviceIntent.setAction(action);

        long taskId = intent.getLongExtra(DownloadController.EXTRA_TASK_ID, 0L);
        if (taskId > 0) {
            serviceIntent.putExtra(DownloadController.EXTRA_TASK_ID, taskId);
        }

        DownloadController.startService(context, serviceIntent);
    }
}
