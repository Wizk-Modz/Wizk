package com.wizk.app.download.model;

public class DownloadRequest {

    private String url;
    private String fileName;
    private String userAgent;
    private String contentDisposition;
    private String mimeType;
    private String referer;
    private String destinationPath;
    private long contentLength;

    // Khởi tạo đối tượng DownloadRequest
    public DownloadRequest() {
    }

    public static class Builder {
        private final DownloadRequest request = new DownloadRequest();

        // Thiết lập URL của tệp cần tải
        public Builder setUrl(String url) {
            request.url = url;
            return this;
        }

        // Thiết lập tên hiển thị của tệp
        public Builder setFileName(String fileName) {
            request.fileName = fileName;
            return this;
        }

        // Thiết lập chuỗi User-Agent của phiên duyệt web
        public Builder setUserAgent(String userAgent) {
            request.userAgent = userAgent;
            return this;
        }

        // Thiết lập tiêu đề Content-Disposition nhận từ server
        public Builder setContentDisposition(String contentDisposition) {
            request.contentDisposition = contentDisposition;
            return this;
        }

        // Thiết lập loại MIME của tệp
        public Builder setMimeType(String mimeType) {
            request.mimeType = mimeType;
            return this;
        }

        // Thiết lập trang giới thiệu Referer
        public Builder setReferer(String referer) {
            request.referer = referer;
            return this;
        }

        // Thiết lập đường dẫn lưu trữ tệp trên bộ nhớ
        public Builder setDestinationPath(String path) {
            request.destinationPath = path;
            return this;
        }

        // Thiết lập dung lượng ước tính của tệp
        public Builder setContentLength(long length) {
            request.contentLength = length;
            return this;
        }

        // Xây dựng đối tượng DownloadRequest hoàn chỉnh
        public DownloadRequest build() {
            return request;
        }
    }

    // Lấy URL tải xuống
    public String getUrl() {
        return url;
    }

    // Thiết lập URL tải xuống
    public void setUrl(String url) {
        this.url = url;
    }

    // Lấy tên tệp
    public String getFileName() {
        return fileName;
    }

    // Thiết lập tên tệp
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // Lấy chuỗi User-Agent
    public String getUserAgent() {
        return userAgent;
    }

    // Thiết lập chuỗi User-Agent
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // Lấy tiêu đề Content-Disposition
    public String getContentDisposition() {
        return contentDisposition;
    }

    // Thiết lập tiêu đề Content-Disposition
    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    // Lấy loại MIME của tệp
    public String getMimeType() {
        return mimeType;
    }

    // Thiết lập loại MIME của tệp
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    // Lấy trang Referer
    public String getReferer() {
        return referer;
    }

    // Thiết lập trang Referer
    public void setReferer(String referer) {
        this.referer = referer;
    }

    // Lấy đường dẫn lưu trữ tệp
    public String getDestinationPath() {
        return destinationPath;
    }

    // Thiết lập đường dẫn lưu trữ tệp
    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    // Lấy dung lượng của tệp tính theo byte
    public long getContentLength() {
        return contentLength;
    }

    // Thiết lập dung lượng của tệp tính theo byte
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}
