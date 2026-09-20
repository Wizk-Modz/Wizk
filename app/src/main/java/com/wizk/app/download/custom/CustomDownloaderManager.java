package com.wizk.app.download.custom;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.CookieManager;

import com.wizk.app.download.model.DownloadRequest;

import java.util.ArrayList;
import java.util.List;

public final class CustomDownloaderManager {

    // Lấy danh sách các ứng dụng tải xuống ngoài hiện đang được cài đặt
    public List<DownloaderInfo> getInstalledExternalDownloaders(Context context) {
        List<DownloaderInfo> installedDownloaders = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        for (DownloaderInfo downloader : SupportedDownloaders.getSupportedDownloaders()) {
            if ("system".equals(downloader.getPackageName())) {
                continue;
            }
            if (isPackageInstalled(packageManager, downloader.getPackageName())) {
                installedDownloaders.add(downloader);
            }
        }
        return installedDownloaders;
    }

    // Mở liên kết tải bằng Activity của ứng dụng tải xuống bên thứ ba
    public boolean startExternalDownload(Context context, DownloaderInfo downloader, DownloadRequest request) {
        if (context == null || downloader == null || request == null || TextUtils.isEmpty(request.getUrl())) {
            return false;
        }

        String[] activities = downloader.getEditorActivities();
        if (activities == null || activities.length == 0) {
            return false;
        }

        for (String activityName : activities) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setComponent(new ComponentName(downloader.getPackageName(), activityName));
            intent.putExtra(Intent.EXTRA_TEXT, request.getUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    // Tạo tác vụ tải bằng DownloadManager mặc định của Android
    public boolean startSystemDownload(Context context, DownloadRequest request) {
        if (context == null || request == null || TextUtils.isEmpty(request.getUrl()) || TextUtils.isEmpty(request.getFileName())) {
            return false;
        }

        try {
            DownloadManager.Request systemRequest = new DownloadManager.Request(Uri.parse(request.getUrl()));
            systemRequest.setTitle(request.getFileName());
            systemRequest.setDescription(request.getFileName());
            systemRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            if (!TextUtils.isEmpty(request.getMimeType())) {
                systemRequest.setMimeType(request.getMimeType());
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                systemRequest.allowScanningByMediaScanner();
                systemRequest.setVisibleInDownloadsUi(true);
            }

            addRequestHeaders(systemRequest, request);
            systemRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, request.getFileName());

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager == null) {
                return false;
            }
            downloadManager.enqueue(systemRequest);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    // Kiểm tra package của ứng dụng có tồn tại và được bật hay không
    private boolean isPackageInstalled(PackageManager packageManager, String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.applicationInfo != null && packageInfo.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    // Đồng bộ các header cần thiết của phiên duyệt web vào DownloadManager
    private void addRequestHeaders(DownloadManager.Request systemRequest, DownloadRequest request) {
        if (!TextUtils.isEmpty(request.getUserAgent())) {
            systemRequest.addRequestHeader("User-Agent", request.getUserAgent());
        }
        if (!TextUtils.isEmpty(request.getReferer())) {
            systemRequest.addRequestHeader("Referer", request.getReferer());
        }
        try {
            String cookie = CookieManager.getInstance().getCookie(request.getUrl());
            if (!TextUtils.isEmpty(cookie)) {
                systemRequest.addRequestHeader("Cookie", cookie);
            }
        } catch (Exception ignored) {
        }
    }
}
