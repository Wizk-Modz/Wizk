package com.wizk.app.download.model;

public final class DownloadStatus {

    public static final int NONE = 0;
    public static final int PENDING = 10;
    public static final int RUNNING = 20;
    public static final int PAUSED = 80;
    public static final int CANCELLED = 95;
    public static final int COMPLETED = 190;
    public static final int ERROR = 200;

    // Ngăn khởi tạo thể hiện của lớp hằng số
    private DownloadStatus() {
    }

    // Kiểm tra xem tác vụ đã kết thúc hay chưa
    public static boolean isFinished(int status) {
        return isCompleted(status) || isError(status);
    }

    // Kiểm tra xem tác vụ có gặp lỗi hay không
    public static boolean isError(int status) {
        return status >= 200 && status < 300;
    }

    // Kiểm tra xem tác vụ có đang tạm dừng hay không
    public static boolean isPaused(int status) {
        return status == PAUSED;
    }

    // Kiểm tra xem tác vụ có đang trong quá trình tải hay không
    public static boolean isRunning(int status) {
        return status > 0 && status < 100 && !isPaused(status) && !isCancelled(status);
    }

    // Kiểm tra xem tác vụ đã hoàn thành thành công hay chưa
    public static boolean isCompleted(int status) {
        return status >= 100 && status < 200;
    }

    // Kiểm tra xem tác vụ đã bị hủy hay chưa
    public static boolean isCancelled(int status) {
        return status == CANCELLED;
    }
}
