package com.wizk.app.download.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.wizk.app.download.engine.DownloadUtils;
import com.wizk.app.download.model.DownloadRequest;

import java.io.File;

public final class DownloadController {

    private static final String TAG = "DownloadController";

    public static final String ACTION_START = "com.wizk.app.download.ACTION_START";
    public static final String ACTION_PAUSE = "com.wizk.app.download.ACTION_PAUSE";
    public static final String ACTION_RESUME = "com.wizk.app.download.ACTION_RESUME";
    public static final String ACTION_REDOWNLOAD = "com.wizk.app.download.ACTION_REDOWNLOAD";
    public static final String ACTION_PAUSE_ALL = "com.wizk.app.download.ACTION_PAUSE_ALL";
    public static final String ACTION_RESUME_ALL = "com.wizk.app.download.ACTION_RESUME_ALL";

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_MIME_TYPE = "mime_type";
    public static final String EXTRA_USER_AGENT = "user_agent";
    public static final String EXTRA_REFERER = "referer";
    public static final String EXTRA_CHUNKS = "chunks";

    // Ngăn việc khởi tạo thể hiện của lớp điều khiển
    private DownloadController() {
    }

    // Đưa một yêu cầu tải xuống mới vào hàng đợi của DownloadService
    public static void enqueue(Context context, DownloadRequest request) {
        if (context == null || request == null) {
            return;
        }

        String fileName = request.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            fileName = DownloadUtils.guessFileName(request.getUrl(), request.getContentDisposition(), request.getMimeType());
        }

        File downloadDir = request.getDestinationPath() != null
                ? new File(request.getDestinationPath())
                : DownloadUtils.getDefaultDownloadDir(context);

        File saveFile = DownloadUtils.getUniqueFile(downloadDir, fileName);

        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_TASK_ID, System.currentTimeMillis());
        intent.putExtra(EXTRA_URL, request.getUrl());
        intent.putExtra(EXTRA_NAME, saveFile.getName());
        intent.putExtra(EXTRA_PATH, saveFile.getAbsolutePath());
        intent.putExtra(EXTRA_MIME_TYPE, request.getMimeType());
        intent.putExtra(EXTRA_USER_AGENT, request.getUserAgent());
        intent.putExtra(EXTRA_REFERER, request.getReferer());
        intent.putExtra(EXTRA_CHUNKS, 4);

        startService(context, intent);
    }

    // Gửi lệnh tạm dừng tác vụ tải xuống
    public static void pauseTask(Context context, long taskId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startService(context, intent);
    }

    // Gửi lệnh tiếp tục tác vụ tải xuống
    public static void resumeTask(Context context, long taskId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_RESUME);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startService(context, intent);
    }

    // Gửi lệnh tải lại tác vụ từ đầu
    public static void redownloadTask(Context context, long taskId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_REDOWNLOAD);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startService(context, intent);
    }

    // Gửi lệnh tạm dừng toàn bộ các tác vụ đang tải
    public static void pauseAllTasks(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_PAUSE_ALL);
        startService(context, intent);
    }

    // Gửi lệnh tiếp tục toàn bộ các tác vụ đang tạm dừng
    public static void resumeAllTasks(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_RESUME_ALL);
        startService(context, intent);
    }

    // Khởi động DownloadService tương thích các phiên bản Android
    public static void startService(Context context, Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi động DownloadService", e);
        }
    }
}
