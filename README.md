# Bài tập Live Coding - Validation Pipeline

## 1. Yêu cầu đề bài
Xây dựng pipeline kiểm tra hợp lệ cho 3 bước ban đầu:
- validateUserInformation
- checkCreditCard
- checkBalance

Các tính năng cần làm:
- Chạy tuần tự các bước (mặc định: validateUserInformation -> checkCreditCard -> checkBalance).
- Đổi được thứ tự chạy lúc runtime mà không cần khởi động lại chương trình.
- Gặp bước nào FAIL thì dừng ngay lập tức (fail-fast), không chạy các bước tiếp theo.
- Cho phép đăng ký thêm bước mới (ví dụ checkBlacklist).
- Đảm bảo an toàn khi nhiều luồng vừa chạy vừa đổi thứ tự.

## 2. Cách làm và thiết kế
- Tạo interface `ValidationStep` với 2 hàm `getName()` và `execute()`, mỗi bước validation sẽ implement interface này để code không bị dính chặt vào nhau.
- Tạo class `PipelineConfiguration` bất biến để chứa map các bước đã đăng ký và thứ tự chạy. Như vậy mỗi lần reorder sẽ tạo ra 1 snapshot mới, tránh bị lỗi đọc nửa vời.
- Trong `ValidationPipeline`:
  - Biến `currentConfig` để `volatile` để luồng đọc lấy được snapshot mới nhất mà không cần dùng lock.
  - Khi gọi hàm `execute()`, lưu snapshot vào biến local để nếu luồng khác có đổi thứ tự thì lượt chạy hiện tại vẫn giữ nguyên thứ tự cũ (snapshot isolation).
  - Vòng lặp chạy từng bước, nếu thấy FAIL thì break và return kết quả luôn (fail-fast).
  - Các hàm ghi như `reorder()` và `registerStep()` dùng block `synchronized` để tránh 2 luồng cùng sửa cấu hình làm mất dữ liệu.
  - Có biến `requireAllSteps` để kiểm tra nếu bật true thì khi reorder bắt buộc phải có đủ tất cả các bước, không được bỏ sót bước nào.

## 3. Các kịch bản kiểm thử (demo trong Main.java)
Trong file `Main.java` em đã viết 5 kịch bản chính để chạy demo và assert kiểm tra:
- Kịch bản 1: Tất cả các bước đều PASS -> kết quả trả về PASS, chạy đủ cả 3 bước.
- Kịch bản 2: Bước 1 FAIL -> dừng luôn ở bước 1, bước 2 và bước 3 không bị gọi.
- Kịch bản 3: Bước 1 PASS, bước 2 FAIL -> dừng ở bước 2, bước 3 không bị gọi.
- Kịch bản 4: Đổi thứ tự thành checkBalance -> validateUserInformation -> checkCreditCard, chạy thử để kiểm tra thứ tự thực tế đã đổi đúng.
- Kịch bản 5: Đăng ký thêm bước mới tại runtime (checkBlacklist) thành 4 bước -> chạy thử cả 4 bước thành công mà không cần sửa thuật toán pipeline.

## 4. Cách chạy chương trình
Chạy trực tiếp class Main để kiểm tra 5 kịch bản sau:
- Kịch bản 1: Tất cả các bước đều PASS
- Kịch bản 2: Bước đầu tiên FAIL (Cơ chế Fail-Fast)
- Kịch bản 3: Bước thứ hai FAIL (Dừng giữa chừng)
- Kịch bản 4: Runtime Reorder (Thay đổi thứ tự đang chạy)
- Kịch bản 5: Đăng ký thêm bước mới tại runtime (checkBlacklist)
