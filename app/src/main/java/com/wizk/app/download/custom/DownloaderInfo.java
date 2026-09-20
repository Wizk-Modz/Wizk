package com.wizk.app.download.custom;

public class DownloaderInfo {

    private String packageName;
    private String[] mainActivities;
    private String[] editorActivities;
    private String displayName;

    // Khởi tạo DownloaderInfo chỉ với tên package
    public DownloaderInfo(String packageName) {
        this(packageName, (String[]) null, (String[]) null, null);
    }

    // Khởi tạo DownloaderInfo với một Activity chính và một Activity nhận liên kết
    public DownloaderInfo(String packageName, String mainActivity, String editorActivity, String displayName) {
        this(packageName,
                mainActivity == null ? null : new String[]{mainActivity},
                editorActivity != null ? new String[]{editorActivity} : null,
                displayName);
    }

    // Khởi tạo DownloaderInfo với một Activity chính và mảng các Activity nhận liên kết
    public DownloaderInfo(String packageName, String mainActivity, String[] editorActivities, String displayName) {
        this(packageName,
                mainActivity == null ? null : new String[]{mainActivity},
                editorActivities,
                displayName);
    }

    // Khởi tạo DownloaderInfo với đầy đủ danh sách Activity và tên hiển thị
    public DownloaderInfo(String packageName, String[] mainActivities, String[] editorActivities, String displayName) {
        this.packageName = packageName;
        this.mainActivities = mainActivities;
        this.editorActivities = editorActivities;
        this.displayName = displayName;
    }

    // Lấy tên package của ứng dụng tải xuống
    public String getPackageName() {
        return packageName;
    }

    // Thiết lập tên package của ứng dụng tải xuống
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    // Lấy danh sách Activity chính của ứng dụng
    public String[] getMainActivities() {
        return mainActivities;
    }

    // Thiết lập danh sách Activity chính của ứng dụng
    public void setMainActivities(String[] mainActivities) {
        this.mainActivities = mainActivities;
    }

    // Lấy danh sách Activity nhận liên kết tải xuống
    public String[] getEditorActivities() {
        return editorActivities;
    }

    // Thiết lập danh sách Activity nhận liên kết tải xuống
    public void setEditorActivities(String[] editorActivities) {
        this.editorActivities = editorActivities;
    }

    // Lấy tên hiển thị thân thiện của ứng dụng
    public String getDisplayName() {
        return displayName;
    }

    // Thiết lập tên hiển thị thân thiện của ứng dụng
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
