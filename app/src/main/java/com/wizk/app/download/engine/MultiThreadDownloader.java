package com.wizk.app.download.engine;

import android.os.SystemClock;
import android.util.Log;

import com.wizk.app.download.model.Chunk;
import com.wizk.app.download.model.DownloadStatus;
import com.wizk.app.download.model.DownloadTask;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreadDownloader {

    private static final String TAG = "MultiThreadDownloader";
    private static final long MULTI_THREAD_MIN_SIZE = 1024 * 1024;

    public interface DownloadListener {
        // Nhận sự kiện tiến độ tải xuống thay đổi
        void onProgress(DownloadTask task);

        // Nhận sự kiện tác vụ đã tải xong
        void onCompleted(DownloadTask task);

        // Nhận sự kiện tác vụ tải xuống gặp lỗi
        void onError(DownloadTask task, Throwable error);
    }

    private final DownloadTask task;
    private final DownloadListener listener;
    private final List<Chunk> chunks = new ArrayList<>();
    private final List<Future<?>> runningFutures = new ArrayList<>();
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private ExecutorService threadPool;

    // Khởi tạo bộ tải đa luồng với tác vụ và bộ lắng nghe kết quả
    public MultiThreadDownloader(DownloadTask task, DownloadListener listener) {
        this.task = task;
        this.listener = listener;
    }

    // Bắt đầu tải xuống trong luồng xử lý riêng
    public void start() {
        new Thread(this::runDownload, "WizkDownloader-" + task.getId()).start();
    }

    // Tạm dừng toàn bộ các phân đoạn đang tải
    public void pause() {
        isPaused.set(true);
        task.setStatus(DownloadStatus.PAUSED);
        cancelRunningFutures();
        notifyProgress();
    }

    // Hủy tác vụ và toàn bộ các phân đoạn đang tải
    public void cancel() {
        isCancelled.set(true);
        task.setStatus(DownloadStatus.CANCELLED);
        cancelRunningFutures();
        notifyProgress();
    }

    // Điều phối tải tệp, cập nhật tiến độ và xử lý kết quả cuối cùng
    private void runDownload() {
        task.setStatus(DownloadStatus.RUNNING);
        try {
            DownloadNetworkClient networkClient = new DownloadNetworkClient(task);
            boolean supportsRange = networkClient.inspectServer();
            File destinationFile = prepareDestinationFile(supportsRange);
            prepareChunks(getChunkCount(supportsRange));
            runChunks(networkClient, destinationFile);
            finishDownload(destinationFile);
        } catch (Exception error) {
            handleDownloadError(error);
        } finally {
            shutdownThreadPool();
        }
    }

    // Tạo tệp đích và chuẩn bị kích thước tệp tương ứng với khả năng Range
    private File prepareDestinationFile(boolean supportsRange) throws Exception {
        File destinationFile = new File(task.getPath());
        File parentDirectory = destinationFile.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists() && !parentDirectory.mkdirs()) {
            throw new IllegalStateException("Không thể tạo thư mục tải xuống");
        }

        try (RandomAccessFile outputFile = new RandomAccessFile(destinationFile, "rw")) {
            if (supportsRange && task.getTotalSize() > 0 && outputFile.length() < task.getTotalSize()) {
                outputFile.setLength(task.getTotalSize());
            } else if (!supportsRange) {
                outputFile.setLength(0);
                task.setDownloadedSize(0);
                task.setChunksState(new ArrayList<>());
            }
        }
        return destinationFile;
    }

    // Xác định số luồng tải phù hợp với phản hồi máy chủ và kích thước tệp
    private int getChunkCount(boolean supportsRange) {
        return supportsRange && task.getTotalSize() > MULTI_THREAD_MIN_SIZE ? Math.max(1, task.getChunks()) : 1;
    }

    // Khôi phục phân đoạn tạm dừng hoặc chia tệp thành các phân đoạn mới
    private void prepareChunks(int chunkCount) {
        chunks.clear();
        if (!task.getChunksState().isEmpty()) {
            chunks.addAll(task.getChunksState());
            return;
        }

        long totalSize = task.getTotalSize();
        if (chunkCount == 1 || totalSize <= 0) {
            chunks.add(new Chunk(0, task.getId(), 0, totalSize > 0 ? totalSize : Long.MAX_VALUE));
        } else {
            long chunkSize = totalSize / chunkCount;
            for (int index = 0; index < chunkCount; index++) {
                long offset = index * chunkSize;
                long length = index == chunkCount - 1 ? totalSize - offset : chunkSize;
                chunks.add(new Chunk(index, task.getId(), offset, length));
            }
        }
        task.setChunksState(chunks);
    }

    // Thực thi tất cả phân đoạn tải xuống và theo dõi tiến độ theo thời gian thực
    private void runChunks(DownloadNetworkClient networkClient, File destinationFile) throws Exception {
        threadPool = Executors.newFixedThreadPool(chunks.size());
        CountDownLatch completionLatch = new CountDownLatch(chunks.size());
        runningFutures.clear();

        for (Chunk chunk : chunks) {
            runningFutures.add(threadPool.submit(() -> downloadChunk(networkClient, destinationFile, chunk, completionLatch)));
        }

        monitorProgress(completionLatch);
        if (isStopped()) {
            cancelRunningFutures();
            return;
        }
        completionLatch.await();
    }

    // Tải một phân đoạn và ghi nhận lỗi nếu tác vụ chưa bị dừng chủ động
    private void downloadChunk(DownloadNetworkClient networkClient, File destinationFile, Chunk chunk, CountDownLatch completionLatch) {
        try {
            networkClient.downloadChunk(chunk, destinationFile, this::isStopped);
        } catch (Exception error) {
            if (!isStopped()) {
                task.setError(error);
                Log.e(TAG, "Lỗi tải phân đoạn " + chunk.getId(), error);
            }
        } finally {
            completionLatch.countDown();
        }
    }

    // Theo dõi tiến độ tải xuống và tốc độ theo chu kỳ một giây
    private void monitorProgress(CountDownLatch completionLatch) throws InterruptedException {
        long lastUpdateTime = SystemClock.elapsedRealtime();
        long lastDownloadedBytes = calculateTotalDownloaded();
        while (completionLatch.getCount() > 0 && !isStopped()) {
            Thread.sleep(500);
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - lastUpdateTime < 1000) {
                continue;
            }
            long downloadedBytes = calculateTotalDownloaded();
            task.setDownloadedSize(downloadedBytes);
            task.setSpeed((downloadedBytes - lastDownloadedBytes) * 1000 / (currentTime - lastUpdateTime));
            lastDownloadedBytes = downloadedBytes;
            lastUpdateTime = currentTime;
            notifyProgress();
        }
    }

    // Hoàn tất trạng thái tác vụ sau khi các phân đoạn đã dừng hoặc tải xong
    private void finishDownload(File destinationFile) {
        task.setDownloadedSize(calculateTotalDownloaded());
        if (isPaused.get()) {
            task.setStatus(DownloadStatus.PAUSED);
            notifyProgress();
            return;
        }
        if (isCancelled.get()) {
            task.setStatus(DownloadStatus.CANCELLED);
            notifyProgress();
            return;
        }
        if (task.getError() != null) {
            task.setStatus(DownloadStatus.ERROR);
            notifyError(task.getError());
            return;
        }

        task.setDownloadedSize(task.getTotalSize() > 0 ? task.getTotalSize() : destinationFile.length());
        task.setSpeed(0);
        task.setStatus(DownloadStatus.COMPLETED);
        if (listener != null) {
            listener.onCompleted(task);
        }
    }

    // Ghi nhận lỗi xảy ra ngoài từng phân đoạn tải xuống
    private void handleDownloadError(Exception error) {
        if (isStopped()) {
            return;
        }
        task.setError(error);
        task.setStatus(DownloadStatus.ERROR);
        Log.e(TAG, "Lỗi trong tiến trình tải", error);
        notifyError(error);
    }

    // Kiểm tra xem tác vụ đã được tạm dừng hoặc hủy bỏ hay chưa
    private boolean isStopped() {
        return isPaused.get() || isCancelled.get();
    }

    // Tính tổng số byte đã tải bởi toàn bộ phân đoạn
    private long calculateTotalDownloaded() {
        long totalDownloaded = 0;
        for (Chunk chunk : chunks) {
            totalDownloaded += chunk.getDownloaded();
        }
        return totalDownloaded;
    }

    // Hủy các Future đang chạy và dừng thread pool
    private void cancelRunningFutures() {
        for (Future<?> future : runningFutures) {
            future.cancel(true);
        }
        runningFutures.clear();
        shutdownThreadPool();
    }

    // Đóng thread pool sau khi tác vụ đã hoàn tất hoặc bị dừng
    private void shutdownThreadPool() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }
    }

    // Thông báo tiến độ tải xuống cho listener
    private void notifyProgress() {
        if (listener != null) {
            listener.onProgress(task);
        }
    }

    // Thông báo lỗi tải xuống cho listener
    private void notifyError(Throwable error) {
        if (listener != null) {
            listener.onError(task, error);
        }
    }
}
