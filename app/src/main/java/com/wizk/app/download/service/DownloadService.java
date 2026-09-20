package com.wizk.app.download.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.webkit.CookieManager;

import androidx.core.app.NotificationCompat;

import com.wizk.app.R;
import com.wizk.app.download.engine.DownloadUtils;
import com.wizk.app.download.engine.MultiThreadDownloader;
import com.wizk.app.download.model.DownloadStatus;
import com.wizk.app.download.model.DownloadTask;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadService extends Service implements MultiThreadDownloader.DownloadListener {

    private static final int FOREGROUND_SERVICE_ID = 2001;

    private final Map<Long, DownloadTask> activeTasks = new ConcurrentHashMap<>();
    private final Map<Long, MultiThreadDownloader> downloaders = new ConcurrentHashMap<>();
    private DownloadNotificationManager notificationManager;

    // Khởi tạo Service, trình quản lý thông báo và chạy Foreground Service
    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = DownloadNotificationManager.getInstance(this);
        startServiceForeground();
    }

    // Đưa Service vào chế độ Foreground với thông báo hệ thống
    private void startServiceForeground() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent openAppIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent pi = openAppIntent != null ? PendingIntent.getActivity(this, 0, openAppIntent, flags) : null;

        Notification notification = new NotificationCompat.Builder(this, DownloadNotificationManager.CHANNEL_RUNNING)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.download_notification_service_title))
                .setContentText(getString(R.string.download_notification_service_text))
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(FOREGROUND_SERVICE_ID, notification);
    }

    // Xử lý các lệnh điều khiển nhận từ Intent gửi tới Service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            checkAutoStop();
            return START_STICKY;
        }

        String action = intent.getAction();
        long taskId = intent.getLongExtra(DownloadController.EXTRA_TASK_ID, 0L);

        if (DownloadController.ACTION_START.equals(action)) {
            handleStartAction(intent);
        } else if (DownloadController.ACTION_PAUSE.equals(action)) {
            pauseTask(taskId);
        } else if (DownloadController.ACTION_RESUME.equals(action)) {
            resumeTask(taskId);
        } else if (DownloadController.ACTION_REDOWNLOAD.equals(action)) {
            redownloadTask(taskId);
        } else if (DownloadController.ACTION_PAUSE_ALL.equals(action)) {
            pauseAllTasks();
        } else if (DownloadController.ACTION_RESUME_ALL.equals(action)) {
            resumeAllTasks();
        }

        checkAutoStop();
        return START_STICKY;
    }

    // Trích xuất thông số và khởi tạo tác vụ tải xuống mới từ Intent
    private void handleStartAction(Intent intent) {
        long taskId = intent.getLongExtra(DownloadController.EXTRA_TASK_ID, System.currentTimeMillis());
        String url = intent.getStringExtra(DownloadController.EXTRA_URL);
        String name = intent.getStringExtra(DownloadController.EXTRA_NAME);
        String path = intent.getStringExtra(DownloadController.EXTRA_PATH);
        String mimeType = intent.getStringExtra(DownloadController.EXTRA_MIME_TYPE);
        String userAgent = intent.getStringExtra(DownloadController.EXTRA_USER_AGENT);
        String referer = intent.getStringExtra(DownloadController.EXTRA_REFERER);
        int chunks = intent.getIntExtra(DownloadController.EXTRA_CHUNKS, 4);

        if (url == null || path == null) {
            return;
        }

        DownloadTask task = new DownloadTask(url, name, path);
        task.setId(taskId);
        task.setMimeType(mimeType);
        task.setChunks(chunks);

        if (userAgent != null) {
            task.addHeader("User-Agent", userAgent);
        }
        if (referer != null) {
            task.addHeader("Referer", referer);
        }
        try {
            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null) {
                task.addHeader("Cookie", cookie);
            }
        } catch (Exception ignored) {
        }

        startTask(task);
    }

    // Đưa tác vụ vào danh sách hoạt động và kích hoạt MultiThreadDownloader
    public void startTask(DownloadTask task) {
        activeTasks.put(task.getId(), task);
        MultiThreadDownloader downloader = new MultiThreadDownloader(task, this);
        downloaders.put(task.getId(), downloader);
        downloader.start();
        notificationManager.updateNotification(task);
    }

    // Tạm dừng một tác vụ tải xuống đang chạy
    public void pauseTask(long taskId) {
        MultiThreadDownloader downloader = downloaders.get(taskId);
        if (downloader != null) {
            downloader.pause();
        }
        DownloadTask task = activeTasks.get(taskId);
        if (task != null) {
            task.setStatus(DownloadStatus.PAUSED);
            notificationManager.updateNotification(task);
        }
    }

    // Tiếp tục thực hiện một tác vụ tải xuống đang tạm dừng
    public void resumeTask(long taskId) {
        DownloadTask task = activeTasks.get(taskId);
        if (task != null) {
            task.setStatus(DownloadStatus.PENDING);
            MultiThreadDownloader downloader = new MultiThreadDownloader(task, this);
            downloaders.put(taskId, downloader);
            downloader.start();
        }
    }

    // Xóa tệp hiện tại và tải lại tác vụ từ đầu
    public void redownloadTask(long taskId) {
        DownloadTask task = activeTasks.get(taskId);
        if (task != null) {
            pauseTask(taskId);
            task.setDownloadedSize(0);
            task.setError(null);
            File file = new File(task.getPath());
            if (file.exists()) {
                file.delete();
            }
            resumeTask(taskId);
        }
    }

    // Tạm dừng toàn bộ các tác vụ tải xuống hiện có
    public void pauseAllTasks() {
        for (Long id : downloaders.keySet()) {
            pauseTask(id);
        }
    }

    // Tiếp tục toàn bộ các tác vụ tải xuống đang tạm dừng
    public void resumeAllTasks() {
        for (DownloadTask task : activeTasks.values()) {
            if (DownloadStatus.isPaused(task.getStatus())) {
                resumeTask(task.getId());
            }
        }
    }

    // Nhận cập nhật tiến độ từ MultiThreadDownloader
    @Override
    public void onProgress(DownloadTask task) {
        notificationManager.updateNotification(task);
    }

    // Xử lý khi tác vụ tải xuống hoàn tất thành công
    @Override
    public void onCompleted(DownloadTask task) {
        downloaders.remove(task.getId());
        notificationManager.updateNotification(task);
        DownloadUtils.scanFile(this, new File(task.getPath()), task.getMimeType());
        checkAutoStop();
    }

    // Xử lý khi tác vụ tải xuống gặp sự cố
    @Override
    public void onError(DownloadTask task, Throwable error) {
        downloaders.remove(task.getId());
        notificationManager.updateNotification(task);
        checkAutoStop();
    }

    // Tự dừng Service khi toàn bộ tác vụ đã hoàn tất, lỗi hoặc bị hủy
    private void checkAutoStop() {
        for (DownloadTask task : activeTasks.values()) {
            if (task.getStatus() == DownloadStatus.RUNNING
                    || task.getStatus() == DownloadStatus.PENDING
                    || task.getStatus() == DownloadStatus.PAUSED) {
                return;
            }
        }

        try {
            stopForeground(true);
            stopSelf();
        } catch (Exception ignored) {
        }
    }

    // Không hỗ trợ liên kết IPC qua IBinder
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Hủy bỏ toàn bộ tác vụ tải và giải phóng tài nguyên khi Service bị dừng
    @Override
    public void onDestroy() {
        pauseAllTasks();
        super.onDestroy();
    }
}
