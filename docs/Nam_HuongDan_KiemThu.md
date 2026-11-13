# Hướng dẫn sử dụng và kết quả kiểm thử

Hai module: **Khách thăm** và **Báo hỏng và bảo trì phòng**.

## 1. Chuẩn bị và chạy

Chạy các file SQL trong **MySQL Workbench** (MySQL 8.0.16 trở lên, lưu file ở dạng UTF-8) theo đúng thứ tự:

| Thứ tự | File | Nội dung |
|---|---|---|
| 1 | `database/01_ktx_schema.sql` | Tạo CSDL KTX, các bảng chung và dữ liệu mẫu |
| 2 | `database/03_khach_tham_phan_Nam.sql` | Thêm cột `LuotTham.ThoiGianDi`, dữ liệu mẫu khách thăm |
| 3 | `database/04_truy_van_khach_tham_phan_Nam.sql` | 10 truy vấn thống kê khách thăm (không bắt buộc, dùng để kiểm tra và làm báo cáo) |
| 4 | `database/05_bao_hong_phan_Nam.sql` | Bảng `BaoHong`, `LichSuBaoHong`, trigger, dữ liệu mẫu |
| 5 | `database/06_truy_van_bao_hong_phan_Nam.sql` | 11 truy vấn tra cứu và thống kê báo hỏng (không bắt buộc) |

Chạy giao diện trong VS Code: mở một trong hai file rồi chạy hàm `main`:

- `src/main/java/ktx/ui/KhachThamPanel.java` (module khách thăm)
- `src/main/java/ktx/ui/BaoHongPanel.java` (module báo hỏng)

Nếu MySQL không dùng tài khoản mặc định, đặt các biến môi trường `KTX_DB_URL`, `KTX_DB_USER`, `KTX_DB_PASSWORD`.

Khi tích hợp vào ứng dụng chính, thêm vào `MainFrame`:

```java
tabs.addTab("Khách thăm", new KhachThamPanel());
tabs.addTab("Báo hỏng", new BaoHongPanel());
```

Ngày giờ nhập theo dạng `dd/MM/yyyy` và `dd/MM/yyyy HH:mm` (ví dụ `04/11/2026 09:30`).

## 2. Module khách thăm

| Tab | Cách dùng |
|---|---|
| Khách | Tìm theo số CMT hoặc họ tên. **Thêm khách** và **Sửa** (số CMT là khóa chính nên không sửa được). |
| Ghi nhận lượt thăm | Nhập số CMT khách (tự hiện tên), mã sinh viên được thăm, giờ đến; giờ về bỏ trống nếu khách còn ở. Bảng bên dưới là khách đang ở trong KTX: chọn một dòng rồi bấm **Ghi nhận khách ra về**. |
| Lịch sử | Lọc theo từ khóa (CMT khách, tên khách, mã sinh viên) và khoảng ngày; cả hai đều có thể bỏ trống. |
| Thống kê | Nút chọn nhanh Tuần này, Tuần trước, Tháng này, Tháng trước (tuần tính từ Thứ Hai đến Chủ nhật), hoặc nhập khoảng ngày. Một lần bấm điền 4 bảng: khách đến thăm từng sinh viên kèm số lần đến, tổng số lần mỗi khách, số lượt của từng sinh viên, số lượt theo ngày. |

Quy tắc: khách nhận diện bằng số CMT; một khách thăm được nhiều sinh viên; chỉ ghi nhận khách thăm sinh viên đang ở KTX; một khách không có hai lượt thăm trùng thời gian; giờ về không trước giờ đến; giờ đến, giờ về không ở tương lai; lượt thăm tính vào kỳ thống kê theo giờ đến.

## 3. Module báo hỏng và bảo trì phòng

| Tab | Cách dùng |
|---|---|
| Tạo báo hỏng | Chọn **Sinh viên báo** (nhập mã sinh viên, phòng đang ở được tự gợi ý) hoặc **Nhân viên phát hiện** (nhập tên nhân viên). Chọn phòng, loại sự cố, mức độ, nhập mô tả rồi bấm **Gửi báo hỏng**. Sự cố mức Cao hoặc Khẩn cấp hiện hộp thoại cảnh báo. |
| Danh sách | Lọc theo phòng, mã sinh viên, trạng thái, loại, mức độ, khoảng ngày báo. Nút **Mới tiếp nhận** và **Chưa hoàn thành (ưu tiên nặng)** xếp mức độ nặng lên trước. Dòng sự cố Cao hoặc Khẩn cấp chưa xong được tô màu. Nhấp đúp để xem chi tiết. |
| Cập nhật xử lý | Nhập mã (hoặc chọn từ Danh sách) rồi **Tải báo hỏng**. Chọn trạng thái kế tiếp, nhân viên, thời điểm, ghi chú hoặc kết quả, bấm **Cập nhật**. Báo hỏng gửi nhầm (còn Mới báo) xóa được bằng **Xóa báo hỏng này**. |
| Chi tiết & lịch sử | Thông tin đầy đủ một báo hỏng và các bước xử lý theo thời gian; có cảnh báo nếu mức cao/khẩn cấp chưa xong. |
| Thống kê | Theo loại sự cố, mức độ, tháng (theo ngày báo) và thời gian xử lý trung bình theo mức độ. |

Danh mục (một chỗ duy nhất định nghĩa, ở các `CHECK` của script 05 và lớp `BaoHongDanhMuc`):

- Loại sự cố: Điện, Nước, Thiết bị, Vệ sinh, Khác.
- Mức độ: Thấp, Trung bình, Cao, Khẩn cấp.
- Trạng thái: Mới báo, Đang xử lý, Đã hoàn thành, Từ chối.

Vòng đời: Mới báo → Đang xử lý hoặc Từ chối; Đang xử lý → Đã hoàn thành hoặc Từ chối. Đã hoàn thành và Từ chối là trạng thái kết thúc: không đổi lại được và không xóa được.

Quy tắc khác:

- Sinh viên chỉ báo hỏng cho phòng mình đang ở (hợp đồng thuê còn hiệu lực tại ngày báo). Sự cố do nhân viên phát hiện không có mã sinh viên.
- Khi chuyển sang Đang xử lý bắt buộc có nhân viên; khi kết thúc bắt buộc có nhân viên, kết quả hoặc lý do từ chối.
- Thời điểm xử lý không được trước ngày báo, không trước bước xử lý trước đó và không ở tương lai.
- Mỗi lần tạo hoặc đổi trạng thái ghi một dòng vào `LichSuBaoHong` trong cùng transaction.
- Thời gian xử lý = ngày kết thúc − ngày báo, chỉ tính báo hỏng Đã hoàn thành.

## 4. Kết quả kiểm thử

### 4.1 Đã kiểm tra (không dùng MySQL thật)

Môi trường soạn thảo không có MySQL và không có màn hình đồ họa, nên các mục dưới đây được kiểm tra bằng cách thay thế. **Chúng chứng minh logic và cú pháp truy vấn, chưa thay thế được việc chạy thật ở mục 4.2.**

| Nội dung | Cách kiểm tra | Kết quả |
|---|---|---|
| Truy vấn báo hỏng (11 truy vấn trong script 06) với dữ liệu mẫu của script 05 | Chạy trên SQLite, đối chiếu từng kết quả kỳ vọng ghi trong file (thứ tự, số lượng, thời gian xử lý, kỳ rỗng) | 16/16 đạt |
| Dữ liệu mẫu và ràng buộc script 05 | Chạy trên SQLite: số dòng, chạy lại 2 lần không nhân đôi, nhất quán trạng thái với lịch sử, 11 trường hợp sai phải bị từ chối (mức độ, trạng thái, loại, ngày xử lý trước ngày báo, thiếu nhân viên hoặc kết quả, phòng hoặc sinh viên không tồn tại) | 17/17 đạt |
| Quy tắc nghiệp vụ báo hỏng (`BaoHongRules`) | Chương trình Java riêng: tạo báo hỏng, đổi trạng thái hợp lệ và sai (nhảy cóc, lùi, mở lại), ngày xử lý, nhân viên, kết quả, giới hạn độ dài, giá trị null | 29/29 đạt (đã sửa 1 lỗi null phát hiện được) |
| Repository, service và 2 giao diện chạy thật | Driver JDBC giả có kiểm tra chặt: báo lỗi nếu đọc cột không có trong câu SELECT hoặc số tham số `?` không khớp số lần gán. Chạy 44 câu SQL, mọi phương thức service, tạo 2 panel, chuyển các tab, vẽ các ô bảng, bấm các nút an toàn | 77/77 đạt. Đã thử gây lỗi giả (sai tên cột, thiếu tham số) và bộ kiểm tra phát hiện đúng |
| Truy vấn thống kê khách thăm (script 04) | Chạy trên SQLite với dữ liệu mẫu của script 03 | Đạt (kể cả Q9 theo ngày, Q10 theo tháng) |

### 4.2 Cần chạy trên máy (MySQL thật và giao diện)

Điền cột "Kết quả thực tế" và "Đạt?" khi chạy. Bắt đầu từ CSDL vừa chạy xong script 01, 03, 05.

**Khách thăm**

| # | Thao tác | Kết quả mong đợi | Kết quả thực tế | Đạt? |
|---|---|---|---|---|
| K1 | Thêm khách có số CMT đã tồn tại (ví dụ `GUEST0001`) | Báo "Số CMT ... đã có trong danh sách khách" | | |
| K2 | Thêm khách với ngày sinh `31/02/2000` | Báo sai định dạng ngày | | |
| K3 | Ghi nhận khách thăm sinh viên không tồn tại | Báo sinh viên không tồn tại | | |
| K4 | Ghi nhận khách thăm sinh viên không còn ở KTX | Báo sinh viên không ở trong KTX | | |
| K5 | Ghi nhận khách `GUEST0003` đến khi đang có lượt chưa ghi giờ về (mẫu có sẵn) | Báo trùng khoảng thời gian | | |
| K6 | Ghi nhận khách ra về với giờ về trước giờ đến | Báo giờ về không được trước giờ đến | | |
| K7 | Ghi nhận khách đến hợp lệ, rồi ra về | Lượt thăm xuất hiện ở "Lịch sử" với đúng thời lượng | | |
| K8 | Thống kê Tuần trước/Tuần này quanh Chủ nhật 25/10 và Thứ Hai 26/10/2026 | Lượt Chủ nhật thuộc tuần 19-25/10, lượt Thứ Hai thuộc tuần 26/10-01/11 | | |
| K9 | Thống kê 01-31/10/2026, xem bảng "theo ngày" | 7 ngày, mỗi ngày 1 lượt, 1 khách | | |

**Báo hỏng**

| # | Thao tác | Kết quả mong đợi | Kết quả thực tế | Đạt? |
|---|---|---|---|---|
| B1 | Sinh viên `SV001` báo hỏng cho phòng `P102` (không phải phòng đang ở) | Báo sinh viên không ở phòng này | | |
| B2 | Sinh viên `SV001` báo hỏng đúng phòng `P101`, mức Trung bình | Tạo thành công, trạng thái Mới báo, xuất hiện ở "Mới tiếp nhận" | | |
| B3 | Gửi báo hỏng mức Khẩn cấp | Hiện hộp thoại cảnh báo; dòng tô đỏ trong Danh sách | | |
| B4 | Nhân viên phát hiện, không nhập tên nhân viên | Báo hãy nhập tên nhân viên ghi nhận | | |
| B5 | Chuyển Mới báo → Đang xử lý, nhập nhân viên | Thành công, có thêm 1 dòng lịch sử | | |
| B6 | Chuyển Đang xử lý → Đã hoàn thành, để trống kết quả | Báo hãy nhập kết quả xử lý | | |
| B7 | Cập nhật với thời điểm trước ngày báo | Báo ngày xử lý không được trước ngày báo | | |
| B8 | Hoàn thành với kết quả hợp lệ | Thành công, có ngày xử lý, thời gian xử lý hiển thị ở Danh sách | | |
| B9 | Thử đổi trạng thái báo hỏng đã hoàn thành | Không có trạng thái nào để chọn; ô cập nhật bị khóa | | |
| B10 | Xóa báo hỏng ở trạng thái Mới báo | Xóa được, lịch sử đi kèm cũng mất | | |
| B11 | Xóa báo hỏng Đã hoàn thành (bằng SQL: `DELETE FROM BaoHong WHERE TrangThai = 'Đã hoàn thành' LIMIT 1;`) | Trigger chặn, báo lỗi | | |
| B12 | Mở lại báo hỏng đã hoàn thành bằng SQL (câu (g) cuối script 05) | Trigger chặn, báo lỗi | | |
| B13 | Lọc Danh sách theo phòng, trạng thái, mức độ, khoảng ngày | Kết quả đúng bộ lọc; bỏ trống một đầu của khoảng ngày thì báo lỗi | | |
| B14 | Thống kê tháng 11/2026 | Theo loại, mức độ, tháng khớp với kỳ vọng ghi trong script 06 | | |
| B15 | Thời gian xử lý trung bình kỳ 01/09-30/11/2026 | 3 báo hỏng, trung bình 35,3 giờ | | |

### 4.3 Hạn chế đã biết

- Chưa chạy trên MySQL thật: các `CHECK` cần MySQL 8.0.16 trở lên; phần so sánh nhị phân và trigger chỉ được rà bằng mắt.
- Các script đã đặt `SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci` để tránh lỗi "Illegal mix of collations"; nếu vẫn gặp lỗi này, kiểm tra collation của CSDL KTX.
- Truy vấn chạy trực tiếp trên luồng giao diện (giống code có sẵn của nhóm); với dữ liệu cỡ bài tập thì không ảnh hưởng.
- Nhân viên xử lý là chuỗi tự do vì hệ thống chưa có bảng nhân viên.
