# TASK BREAKDOWN — KTX MANAGEMENT

## 1. Mục tiêu và nguyên tắc phân công

Dự án sử dụng Java Swing, JDBC và MySQL. Mỗi thành viên phụ trách module đã được giao, bao gồm: thiết kế/cập nhật CSDL, nghiệp vụ, truy vấn SQL, Model, Repository/DAO, Service, giao diện Java Swing và kiểm thử.

Nguyên tắc:
- Thành viên tiếp tục phụ trách module nền tảng mình đã làm và phát triển thêm module mới có liên quan.
- Dùng chung một schema MySQL; thống nhất PK/FK và tên cột trước khi tích hợp.
- Không tự ý sửa bảng do người khác phụ trách.
- Mọi thay đổi schema phải cập nhật script SQL và thông báo cho nhóm.
- Giao diện gọi Service; không viết SQL trực tiếp trong sự kiện nút bấm.
- Mỗi thành viên làm trên branch riêng, tạo Pull Request, không push trực tiếp lên `main`.

## 2. Phân công công việc

## BIÊN — Thành viên 1 / Trưởng nhóm
**Module nền tảng: Sinh viên – Phòng – Thuê phòng**

### Phạm vi phụ trách
- Thiết kế CSDL tổng thể, thống nhất PK/FK và quan hệ giữa các module.
- Quản lý sinh viên: danh sách, tìm kiếm, thêm, sửa, xóa có kiểm soát.
- Quản lý phòng: danh sách, thêm/sửa, đơn giá, loại phòng, sức chứa, số người hiện tại.
- Quản lý thuê phòng: xếp phòng, trả phòng, lưu lịch sử thuê.
- Kiểm tra sức chứa và một sinh viên chỉ có một hợp đồng đang hiệu lực.
- Chuẩn bị EER và truy vấn SQL phần cốt lõi.
- Điều phối tích hợp schema và xử lý xung đột khi các module cùng cần cập nhật bảng gốc.

### Các bảng chính
- `SinhVien(MaSV PK, SoCMT UNIQUE, NgaySinh, Lop, QueQuan, ...)`
- `Phong(SoPhong PK, LoaiPhong, DonGia, SoNguoiToiDa, SoNguoiHienTai, ...)`
- `ThuePhong(MaThue PK, MaSV FK, SoPhong FK, NgayBatDau, NgayKetThuc, ...)`

### Quy tắc nghiệp vụ
1. Mã sinh viên và số CMT/CCCD không trùng.
2. Đơn giá không âm; sức chứa lớn hơn 0.
3. Ngày kết thúc thuê không trước ngày bắt đầu.
4. Không xếp phòng nếu sinh viên không tồn tại, đã có hợp đồng đang hiệu lực hoặc phòng đã đầy.
5. Không giảm sức chứa thấp hơn số người hiện tại.
6. Lưu lịch sử thuê; không xóa bản ghi thuê cũ khi sinh viên trả phòng.
7. Dùng transaction/khóa bản ghi phòng khi xếp phòng nếu có khả năng nhiều thao tác đồng thời.

### Sản phẩm bàn giao
- Script SQL schema cốt lõi và phần tích hợp.
- EER/ERD cập nhật.
- Model, Repository/DAO, Service và giao diện Sinh viên/Phòng/Thuê phòng.
- Truy vấn SQL và dữ liệu kiểm thử.
- Hướng dẫn tích hợp các module dùng chung.

---

## MẠNH — Thành viên 2
**Module nền tảng: Dịch vụ – Sử dụng dịch vụ**  
**Module bổ sung: Quản lý hóa đơn tháng**

### Mục tiêu
Quản lý danh mục dịch vụ, ghi nhận việc sử dụng và lập hóa đơn tháng gồm tiền phòng, điện, nước, dịch vụ; theo dõi tình trạng thanh toán.

### Task 2.1 — Dịch vụ và sử dụng dịch vụ
- Quản lý danh sách dịch vụ: thêm, sửa, tra cứu và cập nhật trạng thái cung cấp.
- Ghi nhận sinh viên sử dụng dịch vụ, số lượng, thời điểm và đơn giá áp dụng.
- Lưu đơn giá tại thời điểm sử dụng để không làm thay đổi lịch sử khi giá dịch vụ được cập nhật.
- Tính thành tiền từng lần sử dụng.
- Tra cứu lịch sử sử dụng theo sinh viên, dịch vụ và thời gian.
- Tổng hợp chi phí dịch vụ theo sinh viên trong tháng.

### Task 2.2 — Thiết kế CSDL hóa đơn
- Tiếp tục sử dụng `DichVu` và `SuDungDichVu` hiện có.
- Xây dựng bảng `HoaDon` với các trường dự kiến:
  - `MaHD BIGINT` – khóa chính.
  - `MaSV` – sinh viên được lập hóa đơn.
  - `SoPhong` – phòng tương ứng trong kỳ.
  - `ThangHoaDon DATE` – kỳ hóa đơn, thống nhất cách lưu ngày đầu tháng.
  - `TienPhong DECIMAL(12,2)`.
  - `TienDien DECIMAL(12,2)`.
  - `TienNuoc DECIMAL(12,2)`.
  - `TienDichVu DECIMAL(12,2)`.
  - `TongTien DECIMAL(12,2)`.
  - `TrangThaiThanhToan VARCHAR(20)`.
  - `NgayLap DATETIME`, `NgayThanhToan DATETIME`, `GhiChu VARCHAR(255)`.
- Thiết kế PK/FK, chỉ mục và ràng buộc chống lập hóa đơn trùng kỳ theo quy tắc nhóm thống nhất.
- Thống nhất với Thiện xem phí xe có được gộp vào `TienDichVu` hay cần tách thành khoản riêng; không cộng hai lần.

### Task 2.3 — Xử lý nghiệp vụ hóa đơn
- Lập hóa đơn theo sinh viên/phòng và tháng.
- Lấy tiền phòng từ hợp đồng thuê trong tháng theo quy tắc tính tiền nhóm đã thống nhất.
- Nhận hoặc nhập chỉ số điện/nước theo quy trình nhóm thống nhất; lưu thành tiền tương ứng.
- Tổng hợp tiền dịch vụ từ `SuDungDichVu`.
- Tính `TongTien` bằng tổng các khoản thành phần.
- Theo dõi trạng thái: chưa thanh toán/đã thanh toán (có thể bổ sung quá hạn nếu nhóm thống nhất).
- Khi thanh toán, cập nhật trạng thái và `NgayThanhToan`.
- Không cho phép thanh toán hóa đơn đã thanh toán lần nữa hoặc tạo hóa đơn trùng kỳ.

### Task 2.4 — Truy vấn SQL
- Danh sách hóa đơn theo tháng.
- Hóa đơn theo sinh viên/phòng.
- Tổng doanh thu theo tháng.
- Danh sách hóa đơn chưa thanh toán.
- Chi tiết từng khoản tiền trong hóa đơn.
- Thống kê tổng tiền dịch vụ theo tháng.
- Kiểm tra tổng tiền hóa đơn khớp dữ liệu chi tiết.

### Task 2.5 — Giao diện Java Swing
- Màn hình quản lý/lập hóa đơn tháng.
- Màn hình xem chi tiết hóa đơn.
- Tìm kiếm theo mã sinh viên, phòng, tháng, trạng thái.
- Chức năng xác nhận thanh toán.
- Hiển thị rõ tiền phòng, điện, nước, dịch vụ và tổng tiền.

### Sản phẩm bàn giao
- SQL tạo bảng `HoaDon`, khóa ngoại, chỉ mục và dữ liệu mẫu.
- Model, Repository/DAO, Service và giao diện hóa đơn.
- Các truy vấn thống kê và tra cứu.
- Kết quả kiểm thử lập hóa đơn, chống trùng và thanh toán.
- Hướng dẫn sử dụng module.

---

## THIỆN — Thành viên 3
**Module nền tảng: Xe tháng – Lượt xe**  
**Module bổ sung: Quản lý lưu trú và trạng thái ở**

### Mục tiêu
Tiếp tục quản lý xe tháng; đồng thời xây dựng chức năng theo dõi trạng thái lưu trú, thời hạn hợp đồng và tiền cọc của sinh viên.

### Task 3.1 — Duy trì module xe tháng
- Quản lý xe đăng ký vé tháng và chủ xe.
- Ghi nhận lịch sử lấy/gửi xe.
- Kiểm tra tối đa 2 xe đang đăng ký trên mỗi sinh viên.
- Áp dụng quy tắc miễn phí 2 lượt/ngày và 3.000 đồng/lượt vượt mức theo quy định nhóm.
- Thống kê lượt miễn phí, lượt tính phí và tổng phí phát sinh.
- Trao đổi với Mạnh để thống nhất cách chuyển phí xe sang hóa đơn, tránh tính trùng.

### Task 3.2 — Thiết kế CSDL lưu trú/hợp đồng
- Rà soát bảng `SinhVien`, `Phong`, `ThuePhong` hiện tại trước khi thêm cột.
- Đề xuất bổ sung vào `SinhVien` nếu schema chưa có:
  - `NgayVaoKTX DATE`.
  - `NgayRaKTX DATE`.
  - `TrangThaiLuuTru VARCHAR(20)`.
- Đề xuất bổ sung vào `ThuePhong` nếu schema chưa có:
  - `TienCoc DECIMAL(12,2)`.
  - `TrangThaiHopDong VARCHAR(20)` hoặc `TrangThaiThue VARCHAR(20)` – nhóm phải chọn một trường hoặc quy định rõ mục đích khác nhau trước khi tạo.
  - `NgayKyHopDong DATE`.
  - `NgayHetHanHopDong DATE`.
  - `SoNguoiThamGia INT`.
- `Phong.SoNguoiHienTai` đã thuộc phần dữ liệu phòng cốt lõi; không tạo thêm cột trùng.
- Thiết kế ràng buộc ngày tháng, tiền cọc không âm và số người tham gia hợp lệ.

### Task 3.3 — Xử lý nghiệp vụ lưu trú
- Theo dõi sinh viên đang ở, đã trả phòng, chờ duyệt hoặc hết hạn lưu trú.
- Theo dõi hợp đồng chờ duyệt, đang hiệu lực, đã kết thúc hoặc hết hạn.
- Ghi nhận ngày ký, ngày bắt đầu, ngày hết hạn và tiền cọc.
- Tra cứu danh sách sinh viên đang ở theo phòng.
- Tra cứu hợp đồng sắp hết hạn/hết hạn.
- Khi duyệt hợp đồng, kiểm tra sinh viên và phòng hợp lệ, phòng còn sức chứa.
- Khi trả phòng, cập nhật ngày kết thúc và trạng thái hợp đồng/lưu trú; giữ nguyên lịch sử.
- Phối hợp với Biên để cập nhật số người hiện tại, tránh hai nơi cùng tự ý tăng/giảm số người.
- Không lưu trùng dữ liệu nếu trạng thái hoặc ngày có thể suy ra đáng tin cậy từ `ThuePhong`; thống nhất nguồn dữ liệu chuẩn với Biên.

### Task 3.4 — Truy vấn SQL
- Danh sách sinh viên đang lưu trú.
- Danh sách sinh viên đã trả phòng.
- Danh sách hợp đồng chờ duyệt/đang hiệu lực/hết hạn.
- Hợp đồng sắp hết hạn trong một khoảng thời gian.
- Tra cứu tiền cọc theo sinh viên/phòng.
- Thống kê số người đang ở từng phòng và đối chiếu sức chứa.

### Task 3.5 — Giao diện Java Swing
- Màn hình theo dõi trạng thái lưu trú.
- Màn hình quản lý hợp đồng và tiền cọc.
- Chức năng duyệt hợp đồng, kết thúc hợp đồng/trả phòng theo quyền nghiệp vụ.
- Bộ lọc theo phòng, sinh viên, trạng thái và thời hạn.
- Cảnh báo hợp đồng sắp hết hạn hoặc phòng đã đủ sức chứa.

### Sản phẩm bàn giao
- SQL cập nhật schema lưu trú sau khi được Biên duyệt.
- Model, Repository/DAO, Service và giao diện quản lý lưu trú.
- Truy vấn danh sách đang ở, hợp đồng hết hạn và tiền cọc.
- Kết quả kiểm thử duyệt, hết hạn, trả phòng và kiểm tra sức chứa.
- Tiếp tục bàn giao module xe tháng đã có.

---

## NAM — Thành viên 4
**Module nền tảng: Khách thăm – Lượt thăm**  
**Module bổ sung: Quản lý báo hỏng và bảo trì phòng**

### Mục tiêu
Tiếp tục quản lý khách thăm; xây dựng chức năng để sinh viên báo sự cố phòng và theo dõi quá trình xử lý/bảo trì.

### Task 4.1 — Duy trì module khách thăm
- Quản lý thông tin khách đến thăm.
- Ghi nhận sinh viên được thăm, ngày/giờ đến và lịch sử từng lượt thăm.
- Tra cứu lịch sử theo khách, sinh viên và thời gian.
- Thống kê lượt khách thăm theo ngày/tháng.

### Task 4.2 — Thiết kế CSDL báo hỏng
- Xây dựng bảng `BaoHong` dự kiến gồm:
  - `MaBaoHong BIGINT` – khóa chính.
  - `SoPhong` – khóa ngoại tới `Phong`.
  - `MaSV` – khóa ngoại tới `SinhVien`, có thể cho phép NULL nếu sự cố do nhân viên phát hiện.
  - `LoaiSuCo VARCHAR(100)`.
  - `MoTa TEXT`.
  - `MucDo VARCHAR(20)`.
  - `TrangThai VARCHAR(30)`.
  - `NgayBao DATETIME`.
  - `NgayXuLy DATETIME`.
  - `NhanVienXuLy VARCHAR(100)`.
- Thiết kế PK/FK, chỉ mục tra cứu theo phòng, trạng thái và ngày báo.
- Rà soát cách lưu người báo và người xử lý; không lưu thông tin trùng lặp không cần thiết.

### Task 4.3 — Xử lý nghiệp vụ
- Sinh viên tạo báo cáo sự cố cho phòng đang lưu trú.
- Kiểm tra phòng tồn tại và sinh viên có liên quan đến phòng tại thời điểm báo.
- Phân loại sự cố: điện, nước, thiết bị, vệ sinh hoặc loại khác.
- Chọn mức độ: thấp, trung bình, cao/khẩn cấp theo danh mục thống nhất.
- Theo dõi trạng thái: mới báo, đang xử lý, đã hoàn thành hoặc từ chối.
- Ghi nhận nhân viên xử lý, ngày xử lý và mô tả kết quả.
- Không cho phép ngày xử lý trước ngày báo.
- Lưu lịch sử xử lý, không xóa báo cáo đã hoàn thành.

### Task 4.4 — Truy vấn SQL
- Danh sách báo hỏng mới tiếp nhận.
- Danh sách sự cố đang xử lý/chưa hoàn thành.
- Lịch sử báo hỏng theo phòng.
- Lịch sử báo hỏng theo sinh viên.
- Thống kê sự cố theo loại, mức độ và tháng.
- Thời gian xử lý trung bình nếu dữ liệu ngày báo/ngày xử lý đầy đủ.

### Task 4.5 — Giao diện Java Swing
- Màn hình tạo báo hỏng.
- Màn hình danh sách và lọc báo cáo.
- Màn hình cập nhật trạng thái xử lý.
- Màn hình xem chi tiết và lịch sử bảo trì.
- Hiển thị cảnh báo với sự cố mức độ cao/khẩn cấp.

### Sản phẩm bàn giao
- SQL tạo bảng `BaoHong`, khóa ngoại và dữ liệu mẫu.
- Model, Repository/DAO, Service và giao diện báo hỏng/bảo trì.
- Các truy vấn tra cứu và thống kê.
- Kết quả kiểm thử gửi báo, phân công xử lý, hoàn thành và tra cứu.
- Tiếp tục bàn giao module khách thăm đã có.

---

## 3. Phân quyền chỉnh sửa bảng và phối hợp

| Bảng/nhóm bảng | Người phụ trách chính | Người phối hợp |
|---|---|---|
| `SinhVien`, `Phong`, `ThuePhong` | Biên | Thiện, Nam, Mạnh được đọc và đề xuất thay đổi |
| `DichVu`, `SuDungDichVu`, `HoaDon` | Mạnh | Biên, Thiện |
| `XeThang`, `LuotXe` | Thiện | Mạnh |
| Các bảng lưu trú/hợp đồng mở rộng | Thiện triển khai nghiệp vụ | Biên duyệt thay đổi bảng gốc |
| `Khach`, `LuotTham`, `BaoHong` | Nam | Biên duyệt FK tới bảng gốc |

**Quy tắc quan trọng:** Người phụ trách module không có nghĩa là được tự ý thay đổi cấu trúc bảng gốc. Ví dụ, Thiện đề xuất thêm trường vào `ThuePhong` thì phải thống nhất với Biên trước khi sửa schema; Mạnh cần lấy dữ liệu thuê phòng nhưng không tự sửa bảng `ThuePhong`.

## 4. Quy ước tích hợp mã nguồn

- Mỗi thành viên làm trên branch riêng, ví dụ:
  - `feature/bien-core`
  - `feature/manh-invoices`
  - `feature/thien-residence`
  - `feature/nam-maintenance`
- Không push trực tiếp lên `main`.
- Mỗi Pull Request cần nêu rõ file SQL, class mới/sửa, chức năng và cách kiểm thử.
- Dùng `PreparedStatement` cho truy vấn có tham số.
- Tách Model, Repository/DAO, Service và UI.
- Không để thông tin mật khẩu MySQL trong source code.
- Trước khi merge phải chạy thử với schema chung và dữ liệu mẫu.

## 5. Thứ tự triển khai

1. **Giai đoạn 1 – Chốt schema:** Biên rà soát database hiện tại; Mạnh, Thiện, Nam gửi danh sách bảng/cột cần thêm; cả nhóm thống nhất PK/FK.
2. **Giai đoạn 2 – Phát triển module:** Mạnh làm hóa đơn; Thiện làm lưu trú; Nam làm báo hỏng/bảo trì; Biên duy trì phần lõi và điều phối schema.
3. **Giai đoạn 3 – Kiểm thử riêng:** Mỗi người kiểm thử SQL, nghiệp vụ và giao diện module của mình.
4. **Giai đoạn 4 – Tích hợp:** Tạo Pull Request, rà soát schema và tích hợp vào nhánh chung.
5. **Giai đoạn 5 – Kiểm thử liên module:** Kiểm tra luồng hợp đồng → hóa đơn; dịch vụ → hóa đơn; xe → khoản phí; sinh viên/phòng → báo hỏng.
6. **Giai đoạn 6 – Hoàn thiện:** Cập nhật README, ảnh giao diện, EER và kết quả kiểm thử.

## 6. Tiêu chí hoàn thành module

Một module chỉ được xem là hoàn thành khi:
- Có script SQL tạo/cập nhật bảng và dữ liệu mẫu.
- Có PK/FK và ràng buộc dữ liệu phù hợp.
- Có Model, Repository/DAO, Service và giao diện.
- Các chức năng thêm/sửa/xóa/tìm kiếm hoặc quy trình nghiệp vụ chạy đúng theo phạm vi.
- Có truy vấn SQL phục vụ tra cứu/thống kê.
- Đã kiểm thử trường hợp hợp lệ và không hợp lệ.
- Tích hợp được với schema chung, không làm hỏng module khác.
- Có hướng dẫn chạy và mô tả các chức năng đã thực hiện.
