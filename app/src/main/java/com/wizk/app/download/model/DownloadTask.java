package com.wizk.app.download.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadTask {

    private long id;
    private String name;
    private long downloadedSize;
    private long totalSize;
    private long speed;
    private int status = DownloadStatus.PENDING;
    private String url;
    private int chunks = 4;
    private String path;
    private String mimeType;
    private Uri fileUri;
    private List<Chunk> chunksState = new ArrayList<>();
    private Map<String, String> headers;
    private int flags;
    private Throwable error;
    private long createdAt;
    private long updatedAt;

    // Khởi tạo đối tượng tác vụ tải xuống với thời gian hiện tại
    public DownloadTask() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    // Khởi tạo tác vụ tải xuống với URL, tên hiển thị và đường dẫn lưu
    public DownloadTask(String url, String name, String path) {
        this();
        this.url = url;
        this.name = name;
        this.path = path;
    }

    // Lấy ID định danh của tác vụ tải xuống
    public long getId() {
        return id;
    }

    // Thiết lập ID định danh của tác vụ tải xuống
    public void setId(long id) {
        this.id = id;
    }

    // Lấy tên hiển thị của tệp
    public String getName() {
        return name;
    }

    // Thiết lập tên hiển thị của tệp
    public void setName(String name) {
        this.name = name;
    }

    // Lấy số byte đã tải xong
    public long getDownloadedSize() {
        return downloadedSize;
    }

    // Thiết lập số byte đã tải xong
    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    // Lấy tổng dung lượng tệp tính theo byte
    public long getTotalSize() {
        return totalSize;
    }

    // Thiết lập tổng dung lượng tệp tính theo byte
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    // Lấy tốc độ tải xuống hiện tại tính theo byte trên giây
    public long getSpeed() {
        return speed;
    }

    // Thiết lập tốc độ tải xuống hiện tại
    public void setSpeed(long speed) {
        this.speed = speed;
    }

    // Lấy trạng thái hiện tại của tác vụ
    public int getStatus() {
        return status;
    }

    // Thiết lập trạng thái hiện tại của tác vụ
    public void setStatus(int status) {
        this.status = status;
    }

    // Lấy URL nguồn của tệp cần tải
    public String getUrl() {
        return url;
    }

    // Thiết lập URL nguồn của tệp cần tải
    public void setUrl(String url) {
        this.url = url;
    }

    // Lấy số phân đoạn tải xuống song song
    public int getChunks() {
        return chunks;
    }

    // Thiết lập số phân đoạn tải xuống song song
    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    // Lấy đường dẫn tệp trên bộ nhớ máy
    public String getPath() {
        return path;
    }

    // Thiết lập đường dẫn tệp trên bộ nhớ máy
    public void setPath(String path) {
        this.path = path;
    }

    // Lấy loại MIME của tệp
    public String getMimeType() {
        return mimeType;
    }

    // Thiết lập loại MIME của tệp
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    // Lấy URI lưu tệp
    public Uri getFileUri() {
        return fileUri;
    }

    // Thiết lập URI lưu tệp
    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    // Lấy trạng thái các phân đoạn tải xuống
    public List<Chunk> getChunksState() {
        return chunksState;
    }

    // Thiết lập trạng thái các phân đoạn tải xuống
    public void setChunksState(List<Chunk> chunksState) {
        this.chunksState = chunksState == null ? new ArrayList<>() : chunksState;
    }

    // Lấy danh sách HTTP headers gửi kèm
    public Map<String, String> getHeaders() {
        return headers;
    }

    // Thiết lập danh sách HTTP headers gửi kèm
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    // Thêm một HTTP header vào yêu cầu tải
    public void addHeader(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        if (value == null) {
            this.headers.remove(key);
        } else {
            this.headers.put(key, value);
        }
    }

    // Kiểm tra xem máy chủ có hỗ trợ tải theo phân đoạn Range hay không
    public boolean isRangeSupported() {
        return (flags & 1) == 1;
    }

    // Thiết lập khả năng hỗ trợ phân đoạn Range của máy chủ
    public void setRangeSupported(boolean supported) {
        if (supported) {
            this.flags |= 1;
        } else {
            this.flags &= ~1;
        }
    }

    // Lấy chi tiết ngoại lệ nếu xảy ra lỗi
    public Throwable getError() {
        return error;
    }

    // Thiết lập chi tiết ngoại lệ khi xảy ra lỗi
    public void setError(Throwable error) {
        this.error = error;
    }

    // Lấy thời điểm tạo tác vụ
    public long getCreatedAt() {
        return createdAt;
    }

    // Thiết lập thời điểm tạo tác vụ
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Lấy thời điểm cập nhật trạng thái gần nhất
    public long getUpdatedAt() {
        return updatedAt;
    }

    // Thiết lập thời điểm cập nhật trạng thái gần nhất
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Tính toán tiến độ tải xuống từ 0 đến 100 phần trăm
    public int getProgress() {
        if (totalSize <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.max(0, (downloadedSize * 100) / totalSize));
    }
}
