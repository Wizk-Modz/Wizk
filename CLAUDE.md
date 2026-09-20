# Quy Tắc Phát Triển

Task chỉ hoàn thành khi đã self-review kỹ và khắc phục toàn bộ lỗi.

## 1. Quy trình
**PLAN → BRANCH → IMPLEMENT → SELF-REVIEW → FIX → VERIFY → REPORT**
- Tạo branch trước khi thay đổi. Lặp *Find & Fix* đến khi sạch lỗi.
- Không chỉ báo cáo lỗi mà không sửa. Không thay đổi ngoài scope task.
- Lỗi không sửa được → rollback, thông báo lý do. Không để code nửa chừng.
- Task quá lớn → đề xuất tách nhỏ. Dependency xung đột → báo cụ thể version.

## 2. Comment & Đặt tên
- Mỗi hàm bắt buộc **1 dòng comment** ngay trên định nghĩa, mô tả ngắn gọn chức năng.
- Dùng ký hiệu comment đúng ngôn ngữ (`#` Python/Ruby, `//` JS/TS/Java/Go/Rust/C/PHP, `--` SQL/Lua).
- Đặt tên theo convention chuẩn của ngôn ngữ đang dùng (snake_case, camelCase, PascalCase tương ứng).
- Tên rõ nghĩa, tránh viết tắt mơ hồ (trừ `i`, `ctx`, `err`, `req`, `res`, `db`, `tx`).
- **Ưu tiên convention dự án hiện có** hơn convention mặc định.

## 3. Cấu trúc & Dependency
- Tạo file/thư mục theo cấu trúc chuẩn của ngôn ngữ. Dự án có sẵn cấu trúc → tuân theo, không tự ý tổ chức lại.
- Không tự ý thêm/nâng cấp dependency. Ghi rõ version, tránh `latest`.
- Lock file phải đồng bộ. Kiểm tra tương thích trước khi thêm dependency mới.
- File > 300 dòng → cân nhắc tách module. Sửa một phần → dùng chỉnh sửa cục bộ.

## 4. Review & Checklist
- **Chức năng**: Logic, edge cases, null/exception, nhất quán trạng thái.
- **Chất lượng**: Không dead code / unused import, đặt tên chuẩn.
- **Bảo mật**: Validate input, không hardcode secrets, tránh leak tài nguyên.
- **Build**: Syntax, import, typecheck/lint, tests pass.
- **Tương thích**: Không phá vỡ API/interface hiện có.

Checklist:
- [ ] Build/lint/typecheck OK
- [ ] Không broken import, không sót implementation
- [ ] Không TODO/FIXME còn sót (trừ khi yêu cầu)
- [ ] Tests pass, config hợp lệ
- [ ] Không xoá/ghi đè file ngoài scope
- [ ] Dependency không xung đột, lock file đồng bộ

## 5. Build & Test
- Chạy build/lint/test bằng tool đã cấu hình trong dự án — không tự thêm tool mới.
- Dự án không có test/lint → ghi nhận trong báo cáo, không tự tạo trừ khi được yêu cầu.

## 6. Báo cáo cuối (bắt buộc)
```
Review Summary
Changes Made: ...
Files Reviewed: ...
Issues Found: N
Issues Fixed: ...
Validation Performed: ...
Remaining Risks: None
```

## 7. Commit Message
Tiền tố: `feat:` | `fix:` | `style:` | `refactor:` | `perf:` | `test:` | `docs:` | `ci:` | `chore:`

## 8. Quy tắc cốt lõi
- **Không tự ý commit** khi chưa được yêu cầu.
- **Không xoá/ghi đè file** ngoài scope task.
- **Tuyệt đối không commit secrets/credentials.**
- **Ngôn ngữ**: Trả lời bằng tiếng Việt trừ khi yêu cầu khác.
- **Không chắc chắn** → hỏi lại, không tự đoán.
- Luôn self-review kỹ; có lỗi là sửa triệt để ngay.
