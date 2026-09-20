package com.wizk.app.download.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.wizk.app.R;
import com.wizk.app.download.engine.DownloadUtils;
import com.wizk.app.download.model.DownloadStatus;
import com.wizk.app.download.model.DownloadTask;

public class DownloadNotificationManager {

    public static final String CHANNEL_RUNNING = "com.wizk.app.download.running";
    public static final String CHANNEL_COMPLETED = "com.wizk.app.download.completed";

    private static volatile DownloadNotificationManager sInstance;
    private final Context context;
    private final NotificationManager notificationManager;

    // Lấy thể hiện đơn lệ của DownloadNotificationManager
    public static DownloadNotificationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadNotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadNotificationManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    // Khởi tạo DownloadNotificationManager và đăng ký các kênh thông báo
    private DownloadNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        initChannels();
    }

    // Tạo các kênh thông báo cho tiến trình tải và khi tải hoàn tất
    private void initChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel runningChannel = new NotificationChannel(
                    CHANNEL_RUNNING,
                    "Đang tải xuống",
                    NotificationManager.IMPORTANCE_LOW
            );
            runningChannel.setDescription("Hiển thị tiến độ các tệp đang tải xuống");
            notificationManager.createNotificationChannel(runningChannel);

            NotificationChannel completedChannel = new NotificationChannel(
                    CHANNEL_COMPLETED,
                    "Tải xuống hoàn tất",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            completedChannel.setDescription("Thông báo khi tệp tải về thành công hoặc lỗi");
            notificationManager.createNotificationChannel(completedChannel);
        }
    }

    // Cập nhật thông báo tiến độ, tốc độ hoặc kết quả tải xuống
    public void updateNotification(DownloadTask task) {
        if (task == null) {
            return;
        }
        int status = task.getStatus();
        if (status == DownloadStatus.COMPLETED) {
            showCompletedNotification(task);
        } else if (status == DownloadStatus.ERROR) {
            showErrorNotification(task);
        } else {
            showRunningNotification(task);
        }
    }

    // Hiển thị thông báo khi tệp đang tải kèm nút điều khiển
    private void showRunningNotification(DownloadTask task) {
        int notifId = (int) (task.getId() & 0x7FFFFFFF);
        int progress = task.getProgress();
        boolean isPaused = (task.getStatus() == DownloadStatus.PAUSED);

        String title = task.getName();
        String downloadedStr = DownloadUtils.formatSize(task.getDownloadedSize());
        String totalStr = DownloadUtils.formatSize(task.getTotalSize());
        String speedStr = DownloadUtils.formatSize(task.getSpeed()) + "/s";

        String contentText = isPaused
                ? context.getString(R.string.download_notification_paused, progress)
                : context.getString(R.string.download_notification_progress, progress, downloadedStr, totalStr, speedStr);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_RUNNING)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText(contentText)
                .setOngoing(!isPaused)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (task.getTotalSize() > 0) {
            builder.setProgress(100, progress, false);
        } else {
            builder.setProgress(0, 0, true);
        }

        if (isPaused) {
            Intent resumeIntent = new Intent(context, DownloadReceiver.class);
            resumeIntent.setAction(DownloadController.ACTION_RESUME);
            resumeIntent.putExtra(DownloadController.EXTRA_TASK_ID, task.getId());
            PendingIntent resumePi = PendingIntent.getBroadcast(context, notifId * 2 + 1, resumeIntent, flags);
            builder.addAction(android.R.drawable.ic_media_play, context.getString(R.string.download_action_resume), resumePi);
        } else {
            Intent pauseIntent = new Intent(context, DownloadReceiver.class);
            pauseIntent.setAction(DownloadController.ACTION_PAUSE);
            pauseIntent.putExtra(DownloadController.EXTRA_TASK_ID, task.getId());
            PendingIntent pausePi = PendingIntent.getBroadcast(context, notifId * 2, pauseIntent, flags);
            builder.addAction(android.R.drawable.ic_media_pause, context.getString(R.string.download_action_pause), pausePi);
        }

        notificationManager.notify(notifId, builder.build());
    }

    // Hiển thị thông báo khi tệp đã hoàn tất quá trình tải
    private void showCompletedNotification(DownloadTask task) {
        int notifId = (int) (task.getId() & 0x7FFFFFFF);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_COMPLETED)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(task.getName())
                .setContentText(context.getString(R.string.download_notification_complete, DownloadUtils.formatSize(task.getTotalSize())))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        notificationManager.notify(notifId, notification);
    }

    // Hiển thị thông báo khi xảy ra lỗi trong quá trình tải
    private void showErrorNotification(DownloadTask task) {
        int notifId = (int) (task.getId() & 0x7FFFFFFF);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_COMPLETED)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(task.getName())
                .setContentText(context.getString(R.string.download_notification_failed))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        notificationManager.notify(notifId, notification);
    }

    // Xóa thông báo của tác vụ tải xuống khỏi thanh trạng thái
    public void cancelNotification(long taskId) {
        int notifId = (int) (taskId & 0x7FFFFFFF);
        notificationManager.cancel(notifId);
    }
}
