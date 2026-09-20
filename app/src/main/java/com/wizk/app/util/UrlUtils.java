package com.wizk.app.util;

import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class UrlUtils {

    // Ngăn việc khởi tạo thể hiện của lớp tiện ích
    private UrlUtils() {
    }

    // Kiểm tra xem chuỗi nhập vào có phải URL hoặc tên miền hợp lệ
    public static boolean isValidWebUrl(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return !input.contains(" ") && (Patterns.WEB_URL.matcher(input).matches() || input.contains("."));
    }

    // Định dạng chuỗi nhập thành liên kết web hoặc truy vấn tìm kiếm
    public static String formatUrlOrSearch(String input, String searchTemplate) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("file://") || input.startsWith("about:")) {
            return input;
        }

        if (isValidWebUrl(input)) {
            return "https://" + input;
        }

        try {
            String encodedQuery = URLEncoder.encode(input, "UTF-8");
            return String.format(searchTemplate, encodedQuery);
        } catch (UnsupportedEncodingException e) {
            return "https://www.google.com/search?q=" + input;
        }
    }

    // Ẩn bàn phím ảo của hệ thống sau khi hoàn tất nhập liệu
    public static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
