# Quy Tắc Phát Triển — Implementation, Review & Fix

Áp dụng cho mọi tác vụ: code, refactor, fix bug, config, dependency, DB, API, tài liệu. Task chỉ hoàn thành khi đã self-review kỹ và khắc phục toàn bộ lỗi phát hiện.

---

## 1. Quy trình bắt buộc
**PLAN → IMPLEMENT → SELF-REVIEW → FIX ISSUES → VERIFY → REPORT**
- Lặp lại vòng lặp *Find & Fix* cho đến khi sạch lỗi.
- Tuyệt đối không dừng lại ở mức "chỉ báo cáo lỗi mà không sửa".

---

## 2. Comment hàm
- Mọi hàm/phương thức bắt buộc có **duy nhất 1 dòng `#` comment** ngay trên định nghĩa.
- Chỉ mô tả ngắn gọn hàm làm gì, không viết dài dòng hay giải thích hiển nhiên.
- Ví dụ:
```python
# Gửi tin nhắn vào kênh chat
def _reply(self, cid, tp, text):
    ...
```

---

## 3. Tiêu chí Review bắt buộc
Kiểm tra toàn diện các file thay đổi và liên quan:
- **Chức năng**: Logic, điều kiện biên (edge cases), xử lý null/exception, tính nhất quán trạng thái.
- **Chất lượng**: Code sạch, không dead code / unused import, đặt tên chuẩn, cấu trúc rõ ràng.
- **Bảo mật & Hiệu năng**: Validate input, không hardcode credentials/secrets, tránh leak tài nguyên/memory leak.
- **Build & Tích hợp**: Syntax chuẩn, import đúng, typecheck/lint pass, tests pass (nếu có).

---

## 4. Checklist hoàn thành
- [x] Syntax, lint, typecheck và build OK, không còn lỗi.
- [x] Không broken imports, không sót implementation.
- [x] Không để lại TODO/FIXME (trừ khi được yêu cầu).
- [x] Không còn review finding chưa xử lý.
- [x] Tests pass (nếu có) và config hợp lệ.

---

## 5. Báo cáo cuối (bắt buộc)
Mọi task kết thúc bằng format:

```
Review Summary

Files Reviewed
- ...

Issues Found
- N

Issues Fixed
- ...

Validation Performed
- ...

Remaining Risks
- None (hoặc liệt kê rủi ro còn lại)
```

---

## 6. Commit Message

When you make changes to one or more files, you need to commit those changes with a commit message. Here are some guidelines:

- Keep the commit message short and detailed.
- Use one of these commit types as a prefix:
  - `feat:` for a feature, possibly improving something already existing.
  - `fix:` for a fix, such as a bug fix.
  - `style:` for features and updates related to styling.
  - `refactor:` for refactoring a specific section of the codebase.
  - `test:` for everything related to testing.
  - `docs:` for everything related to documentation.
  - `chore:` for code maintenance (you can also use emojis to represent commit types).

Examples:
- `feat: Speed up compiling with new technique`
- `fix: Fix crash during launch on certain phones`
- `refactor: Reformat code in File.java`

## 7. Quy tắc cốt lõi & Giao tiếp
- **Không tự ý commit** khi chưa có yêu cầu rõ ràng từ người dùng.
- **Tuyệt đối không commit secrets/credentials** (token, mật khẩu, API key, file nhạy cảm).
- **Ngôn ngữ**: Trả lời bằng tiếng Việt trừ khi người dùng yêu cầu khác.
- **Nguyên tắc**: Luôn self-review kỹ trước khi bàn giao; có lỗi là sửa triệt để ngay.
