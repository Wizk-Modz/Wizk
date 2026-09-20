package com.wizk.app.download.ui;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wizk.app.R;
import com.wizk.app.download.custom.CustomDownloaderManager;
import com.wizk.app.download.custom.DownloaderInfo;
import com.wizk.app.download.engine.DownloadUtils;
import com.wizk.app.download.model.DownloadRequest;
import com.wizk.app.download.service.DownloadController;

import java.util.ArrayList;
import java.util.List;

public final class DownloadDialog {

    // Ngăn việc khởi tạo thể hiện của lớp hộp thoại tải xuống
    private DownloadDialog() {
    }

    // Hiển thị các phương thức tải khả dụng cho yêu cầu tải xuống
    public static void show(Context context, DownloadRequest request) {
        CustomDownloaderManager downloaderManager = new CustomDownloaderManager();
        List<DownloaderInfo> externalDownloaders = downloaderManager.getInstalledExternalDownloaders(context);
        List<String> options = buildOptions(context, externalDownloaders);
        String fileName = request.getFileName();
        String size = request.getContentLength() > 0
                ? DownloadUtils.formatSize(request.getContentLength())
                : context.getString(R.string.download_unknown_size);

        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.download_dialog_title))
                .setMessage(context.getString(R.string.download_dialog_message, fileName, size))
                .setItems(options.toArray(new String[0]), (dialog, selectedIndex) ->
                        startSelectedDownload(context, request, downloaderManager, externalDownloaders, selectedIndex))
                .setNegativeButton(R.string.download_dialog_cancel, null)
                .show();
    }

    // Tạo danh sách các lựa chọn tải xuống cho giao diện
    private static List<String> buildOptions(Context context, List<DownloaderInfo> externalDownloaders) {
        List<String> options = new ArrayList<>();
        options.add(context.getString(R.string.download_option_builtin));
        options.add(context.getString(R.string.download_option_system));
        for (DownloaderInfo downloader : externalDownloaders) {
            options.add(downloader.getDisplayName());
        }
        return options;
    }

    // Thực thi phương thức tải xuống mà người dùng đã chọn
    private static void startSelectedDownload(
            Context context,
            DownloadRequest request,
            CustomDownloaderManager downloaderManager,
            List<DownloaderInfo> externalDownloaders,
            int selectedIndex
    ) {
        boolean started;
        if (selectedIndex == 0) {
            DownloadController.enqueue(context, request);
            return;
        }
        if (selectedIndex == 1) {
            started = downloaderManager.startSystemDownload(context, request);
        } else {
            DownloaderInfo downloader = externalDownloaders.get(selectedIndex - 2);
            started = downloaderManager.startExternalDownload(context, downloader, request);
        }
        if (!started) {
            android.widget.Toast.makeText(context, R.string.download_failed, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
