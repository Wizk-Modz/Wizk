package com.wizk.app.download.model;

public class Chunk {

    private long id;
    private long taskId;
    private long offset;
    private long length;
    private long downloaded;
    private long connectionId;

    // Khởi tạo Chunk rỗng
    public Chunk() {
    }

    // Khởi tạo Chunk với các thông số phân đoạn byte
    public Chunk(long id, long taskId, long offset, long length) {
        this.id = id;
        this.taskId = taskId;
        this.offset = offset;
        this.length = length;
        this.downloaded = 0;
    }

    // Vị trí byte bắt đầu cần tải tiếp của phân đoạn
    public long getBegin() {
        return offset + downloaded;
    }

    // Vị trí byte kết thúc của phân đoạn
    public long getEnd() {
        return (offset + length) - 1;
    }

    // Lấy ID định danh của phân đoạn
    public long getId() {
        return id;
    }

    // Thiết lập ID định danh của phân đoạn
    public void setId(long id) {
        this.id = id;
    }

    // Lấy ID của tác vụ tải xuống cha
    public long getTaskId() {
        return taskId;
    }

    // Thiết lập ID của tác vụ tải xuống cha
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    // Lấy vị trí offset bắt đầu trong toàn bộ tệp
    public long getOffset() {
        return offset;
    }

    // Thiết lập vị trí offset bắt đầu trong toàn bộ tệp
    public void setOffset(long offset) {
        this.offset = offset;
    }

    // Lấy độ dài byte của phân đoạn
    public long getLength() {
        return length;
    }

    // Thiết lập độ dài byte của phân đoạn
    public void setLength(long length) {
        this.length = length;
    }

    // Lấy số byte đã tải xong của phân đoạn này
    public long getDownloaded() {
        return downloaded;
    }

    // Thiết lập số byte đã tải xong của phân đoạn này
    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    // Lấy ID kết nối mạng
    public long getConnectionId() {
        return connectionId;
    }

    // Thiết lập ID kết nối mạng
    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }
}
